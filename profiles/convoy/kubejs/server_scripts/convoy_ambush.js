// The standard bridge tries to load a client class on dedicated servers.
const ConvoyQuestFile = Java.loadClass('dev.ftb.mods.ftbquests.quest.ServerQuestFile')
const ConvoyEncounters = Java.loadClass('net.ssnec.encounter.scenario.EncounterManager')
const ConvoyBlockPos = Java.loadClass('net.minecraft.core.BlockPos')

const lostConvoy = {
  command: 'swa_convoy_ambush',
  encounter: 'lost_convoy',
  x: 144,
  active: 'swa_convoy_active',
  clearPrefix: 'swa_convoy_clear_',
  task: '5EA5020000000031'
}
const relayAmbush = {
  command: 'swa_relay_ambush',
  encounter: 'false_callsign',
  x: 288,
  active: 'swa_relay_active',
  clearPrefix: 'swa_relay_clear_',
  task: '5EA5050000000031'
}

function completeConvoyTask(player, task) {
  player.getServer().runCommandSilent('ftbquests change_progress ' + player.username + ' complete ' + task)
}

function creditConvoyClear(player, ambush) {
  var team = ConvoyQuestFile.INSTANCE.getTeamData(player).orElse(null)
  if (team !== null && player.getServer().persistentData.getBoolean(ambush.clearPrefix + team.getTeamId())) {
    completeConvoyTask(player, ambush.task)
  }
}

function startConvoyAmbush(player, level, ambush, team) {
  var server = player.getServer()
  var state = server.persistentData
  var teamId = team.getTeamId().toString()
  if (state.contains(ambush.active) || state.getBoolean(ambush.clearPrefix + teamId)) return 0

  try {
    ConvoyEncounters.get(server).start(ambush.encounter, level, new ConvoyBlockPos(ambush.x, 140, 0))
    state.putString(ambush.active, teamId)
    return 1
  } catch (error) {
    console.error(ambush.encounter + ' could not start: ' + error)
    return 0
  }
}

function finishConvoyAmbush(context, ambush, cleared) {
  var server = context.getSource().getServer()
  var state = server.persistentData
  var teamId = state.getString(ambush.active)
  if (teamId === '') return 0
  state.remove(ambush.active)
  if (cleared) {
    state.putBoolean(ambush.clearPrefix + teamId, true)
    for (var player of server.getPlayerList().getPlayers()) {
      creditConvoyClear(player, ambush)
    }
  }
  return 1
}

PlayerEvents.loggedIn(event => {
  creditConvoyClear(event.player, lostConvoy)
  creditConvoyClear(event.player, relayAmbush)
})

function convoyAmbushCommand(commands, ambush) {
  return commands.literal(ambush.command)
    .requires(source => source.hasPermission(2))
    .then(commands.literal('clear').executes(context => finishConvoyAmbush(context, ambush, true)))
    .then(commands.literal('abort').executes(context => finishConvoyAmbush(context, ambush, false)))
}

ServerEvents.commandRegistry(event => {
  event.register(convoyAmbushCommand(event.commands, lostConvoy)
    .then(event.commands.literal('start').executes(context => {
      var source = context.getSource()
      var player = source.getPlayerOrException()
      var team = ConvoyQuestFile.INSTANCE.getTeamData(player).orElse(null)
      return team === null ? 0 : startConvoyAmbush(player, source.getLevel(), lostConvoy, team)
    })))
  event.register(convoyAmbushCommand(event.commands, relayAmbush))
})

function completedRelayQuest(team, questId) {
  for (var chapter of ConvoyQuestFile.INSTANCE.getAllChapters()) {
    if (String(chapter.getCodeString()) !== '5EA5050000000001') continue
    for (var quest of chapter.getQuests()) {
      if (String(quest.getCodeString()) === questId) return team.isCompleted(quest)
    }
  }
  return false
}

function useRelayConsole(player, block) {
  var team = ConvoyQuestFile.INSTANCE.getTeamData(player).orElse(null)
  if (team === null || !completedRelayQuest(team, '5EA5050000000010')) {
    player.tell('Диспетчер: сначала примите задание «Запись без позывного».')
    return
  }

  var state = player.getServer().persistentData
  var teamId = team.getTeamId().toString()
  var cleared = state.getBoolean(relayAmbush.clearPrefix + teamId)
  if (block.getX() === -11) {
    if (!completedRelayQuest(team, '5EA5050000000040')) {
      player.tell('Диспетчер: сначала снимите номер партии с пульта на перевалке.')
      return
    }
    completeConvoyTask(player, '5EA5050000000051')
    player.tell('Диспетчер: ведомость принята. Маршрут для гражданского транспорта готов.')
    return
  }

  if (block.getX() === 436) {
    if (!cleared) {
      player.tell('Ведомость заперта. Диспетчеру нужен журнал передачи с Р-17.')
      return
    }
    completeConvoyTask(player, '5EA5050000000041')
    player.tell('Ведомость: партия медгруза за печатью Новой Республики. Получатель указан под вашим старым позывным. Передайте список диспетчеру.')
    return
  }

  if (cleared) {
    player.tell('Журнал уже у диспетчера. Проверьте ведомость на голубой площадке к востоку.')
  } else if (state.contains(relayAmbush.active)) {
    player.tell('Р-17 сейчас передаёт журнал. Дождитесь окончания сеанса.')
  } else if (startConvoyAmbush(player, block.getLevel(), relayAmbush, team)) {
    completeConvoyTask(player, '5EA5050000000021')
  } else {
    player.tell('Р-17 временно недоступен. Повторите попытку с площадки.')
  }
}

BlockEvents.rightClicked('minecraft:lodestone', event => {
  var block = event.getBlock()
  var consoleX = block.getX()
  var consoleZ = block.getZ()
  var dispatchDesk = consoleX === -11 && block.getY() === 140 && consoleZ === 12
  var fieldConsole = (consoleX === 288 || consoleX === 436) && block.getY() === 140 && consoleZ === 0
  if ((!dispatchDesk && !fieldConsole) ||
      String(block.getDimension()) !== 'starwarsmod:tatooine' || String(event.getHand()) !== 'MAIN_HAND') return

  useRelayConsole(event.player, block)
  event.cancel()
})
