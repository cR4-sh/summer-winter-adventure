const HothQuests = Java.loadClass('dev.ftb.mods.ftbquests.quest.ServerQuestFile')
const HothTag = Java.loadClass('net.minecraft.nbt.CompoundTag')
const HothPos = Java.loadClass('net.minecraft.core.BlockPos')
const HothLevelKey = Java.loadClass('net.minecraft.resources.ResourceKey')
const HothResourceId = Java.loadClass('net.minecraft.resources.ResourceLocation')
const HothRegistries = Java.loadClass('net.minecraft.core.registries.Registries')
const HothBlocks = Java.loadClass('net.minecraft.core.registries.BuiltInRegistries').BLOCK
const HothHeightmap = Java.loadClass('net.minecraft.world.level.levelgen.Heightmap$Types')
const HothResearch = Java.loadClass('dev.swa.research.ConvoyResearch')

const echoStateKey = 'swa_research:hoth_echo'
const echoWorld = 'starwarsmod:hoth'
const echoDispatchWorld = 'starwarsmod:tatooine'
const echoVersion = 1
const echoMission = '5EA5050000000050'
const echoTasks = {
  briefed: '5EA5060000000011',
  signal: '5EA5060000000021',
  shelter: '5EA5060000000031',
  reported: '5EA5060000000041',
  kit: '5EA5060000000051'
}
const echoPanels = {
  'minecraft:chiseled_bookshelf': [[-2, 1, 4, 'registry']],
  'minecraft:copper_bulb': [[0, 1, 4, 'power']],
  'minecraft:cartography_table': [[2, 1, 4, 'evacuation']],
  'minecraft:lodestone': [[9, 1, 0, 'east'], [-9, 1, 0, 'west']],
  'minecraft:smooth_stone': [[7, 1, 9, 'kit_inspect']],
  'minecraft:chiseled_copper': [[10, 1, 8, 'field_kit']],
  'swa_research:station': [
    [-3, 1, -1, 'enroll'], [-2, 1, -5, 'control_glove'], [0, 1, -5, 'historical_glove'],
    [-3, 1, -3, 'predict_material'], [3, 1, -3, 'predict_personal_history'],
    [2, 1, -5, 'comparison_token'], [5, 1, 8, 'ship_buckle']
  ]
}

function echoLevel(server, name) {
  const key = HothLevelKey.create(HothRegistries.DIMENSION, HothResourceId.parse(name))
  return server['getLevel(net.minecraft.resources.ResourceKey)'](key)
}

function echoState(server) {
  const root = server.persistentData
  if (!root.contains(echoStateKey)) {
    const state = new HothTag()
    state.putInt('version', echoVersion)
    root.put(echoStateKey, state)
  }
  const state = root.getCompound(echoStateKey)
  if (state.getInt('version') !== echoVersion) {
    throw new Error('Unknown Hoth Echo save format: ' + state.getInt('version'))
  }
  return state
}

function echoPosition(data) {
  return new HothPos(data.getInt('x'), data.getInt('y'), data.getInt('z'))
}

function putEchoPosition(data, world, x, y, z) {
  data.putString('dimension', world)
  data.putInt('x', x)
  data.putInt('y', y)
  data.putInt('z', z)
}

function echoBlockId(level, pos) {
  return String(HothBlocks.getKey(level.getBlockState(pos).getBlock()))
}

function echoTell(player, key, args) {
  player.tell(Text.translate('message.swa_research.' + key, args || []))
}

function echoTeam(player) {
  const file = HothQuests.INSTANCE
  return file === null ? null : file.getTeamData(player).orElse(null)
}

function echoCanStart(team) {
  if (team === null) return false
  for (const chapter of HothQuests.INSTANCE.getAllChapters()) {
    if (String(chapter.getCodeString()) !== '5EA5050000000001') continue
    for (const quest of chapter.getQuests()) {
      if (String(quest.getCodeString()) === echoMission) return team.isCompleted(quest)
    }
  }
  return false
}

function echoCase(state, team, create) {
  const cases = state.getCompound('cases')
  const id = String(team.getTeamId())
  if (!cases.contains(id)) {
    if (!create) return null
    const opened = new HothTag()
    opened.putString('shelter_side', team.getTeamId().hashCode() % 2 === 0 ? 'east' : 'west')
    opened.putBoolean('briefed', true)
    cases.put(id, opened)
    state.put('cases', cases)
  }
  return cases.getCompound(id)
}

function echoCredit(player, which) {
  player.getServer().runCommandSilent('ftbquests change_progress ' + player.username + ' complete ' + echoTasks[which])
}

function echoMark(player, entry, which) {
  entry.putBoolean(which, true)
  echoCredit(player, which)
}

function prepareEchoDispatch(server, state) {
  if (state.contains('dispatch')) return state.getCompound('dispatch')
  const level = echoLevel(server, echoDispatchWorld)
  if (level === null) return null
  level.getChunk(-1, 0)
  for (const x of [-8, -7, -6, -5]) {
    var deskCandidate = new HothPos(x, 140, 12)
    if (!level.getBlockState(deskCandidate).isAir()) continue
    server.runCommandSilent('execute in ' + echoDispatchWorld + ' run setblock ' + x + ' 140 12 minecraft:chiseled_bookshelf[facing=south] keep')
    if (echoBlockId(level, deskCandidate) !== 'minecraft:chiseled_bookshelf') continue
    server.runCommandSilent('execute in ' + echoDispatchWorld + ' run setblock ' + x + ' 141 12 minecraft:spruce_sign[rotation=0]{is_waxed:1b,front_text:{messages:[\'{"text":"ЗАПРОС С ХОТА"}\',\'{"text":"Архив диспетчера"}\',\'{"text":""}\',\'{"text":""}\'],color:"white",has_glowing_text:1b}} keep')
    const dispatch = new HothTag()
    putEchoPosition(dispatch, echoDispatchWorld, x, 140, 12)
    state.put('dispatch', dispatch)
    return dispatch
  }
  return null
}

function chooseEchoSite(level) {
  // Use the highest occupied block in each column so a copy of an existing world
  // never needs clearing. Stay near level ground and leave the entire roof in range.
  for (const [x, z] of [[528, 0], [528, 80], [608, 0], [608, 80], [448, 80], [688, 0]]) {
    for (let cx = (x - 12) >> 4; cx <= (x + 12) >> 4; cx++) {
      for (let cz = (z - 10) >> 4; cz <= (z + 11) >> 4; cz++) level.getChunk(cx, cz)
    }
    let low = level.getMaxBuildHeight()
    let high = level.getMinBuildHeight()
    for (let px = x - 12; px <= x + 12; px++) {
      for (let pz = z - 10; pz <= z + 11; pz++) {
        const y = level.getHeight(HothHeightmap.WORLD_SURFACE, px, pz)
        low = Math.min(low, y)
        high = Math.max(high, y)
      }
    }
    if (high - low > 10 || high + 9 >= level.getMaxBuildHeight()) continue
    return [x, high + 2, z]
  }
  return null
}

function ensureEchoSite(server, state) {
  const level = echoLevel(server, echoWorld)
  if (level === null) return null
  if (!state.contains('site')) {
    const position = chooseEchoSite(level)
    if (position === null) return null
    const site = new HothTag()
    putEchoPosition(site, echoWorld, position[0], position[1], position[2])
    site.putInt('siteVersion', echoVersion)
    state.put('site', site)
  }
  const site = state.getCompound('site')
  if (site.getInt('siteVersion') !== echoVersion || String(site.getString('dimension')) !== echoWorld) return null
  if (site.getBoolean('ready')) return site
  const pos = echoPosition(site)
  const x = pos.getX(), y = pos.getY(), z = pos.getZ()
  for (let cx = (x - 12) >> 4; cx <= (x + 12) >> 4; cx++) {
    for (let cz = (z - 10) >> 4; cz <= (z + 11) >> 4; cz++) level.getChunk(cx, cz)
  }
  server.runCommandSilent('execute in ' + echoWorld + ' positioned ' + x + ' ' + y + ' ' + z + ' run function swa_research:hoth/build')
  for (const id of Object.keys(echoPanels)) {
    for (const [dx, dy, dz] of echoPanels[id]) {
      if (echoBlockId(level, pos.offset(dx, dy, dz)) !== id) return null
    }
  }
  const ground = level.getHeight(HothHeightmap.WORLD_SURFACE, x, z + 11)
  if (ground <= y) {
    const prefix = 'execute in ' + echoWorld + ' run fill ' + x + ' ' + ground + ' '
    server.runCommandSilent(prefix + (z + 10) + ' ' + x + ' ' + y + ' ' + (z + 10) + ' minecraft:polished_deepslate replace minecraft:air')
    server.runCommandSilent(prefix + (z + 11) + ' ' + x + ' ' + y + ' ' + (z + 11) + ' minecraft:ladder[facing=south] replace minecraft:air')
  }
  site.putBoolean('ready', true)
  return site
}

function useEchoDispatch(player, state, team) {
  if (!echoCanStart(team)) {
    echoTell(player, 'dispatch_locked')
    return
  }
  const site = ensureEchoSite(player.getServer(), state)
  if (site === null) {
    echoTell(player, 'site_unavailable')
    return
  }
  const entry = echoCase(state, team, true)
  const pos = echoPosition(site)
  const coordinates = [String(pos.getX()), String(pos.getY() + 1), String(pos.getZ())]
  if (entry.getBoolean('shelter')) {
    echoMark(player, entry, 'reported')
    echoTell(player, 'report_' + String(entry.getString('shelter_side')))
    echoTell(player, 'coordinates', coordinates)
  } else {
    echoCredit(player, 'briefed')
    echoTell(player, 'briefing', coordinates)
  }
}

function useEchoPanel(player, entry, action) {
  switch (action) {
    case 'registry':
      if (!entry.getBoolean('power')) {
        echoTell(player, 'recorder_off')
        return
      }
      echoMark(player, entry, 'signal')
      player.getServer().runCommandSilent('execute as ' + player.username + ' at @s run playsound swa_research:double_knock block @s ~ ~ ~ 0.85 1')
      echoTell(player, 'recorder_signal')
      return
    case 'power':
      entry.putBoolean('power', true)
      echoTell(player, 'recorder_powered')
      return
    case 'evacuation':
      entry.putBoolean('diagram', true)
      echoTell(player, 'wiring_' + String(entry.getString('shelter_side')))
      return
    case 'east':
    case 'west':
      if (!entry.getBoolean('signal') || !entry.getBoolean('diagram')) {
        echoTell(player, 'shelter_clues')
      } else if (String(entry.getString('shelter_side')) === action) {
        entry.putString('inspected', action)
        echoMark(player, entry, 'shelter')
        echoTell(player, 'shelter_found')
      } else {
        entry.putString('inspected', action)
        echoTell(player, 'shelter_empty')
      }
      return
    case 'kit_inspect':
      entry.putBoolean('kit_clue', true)
      echoTell(player, 'kit_clue')
      return
    case 'field_kit':
      if (!entry.getBoolean('shelter') || !entry.getBoolean('kit_clue')) {
        echoTell(player, 'kit_locked')
        return
      }
      echoMark(player, entry, 'kit')
      echoTell(player, 'kit_reward')
  }
}

ServerEvents.loaded(event => prepareEchoDispatch(event.server, echoState(event.server)))

PlayerEvents.loggedIn(event => {
  const player = event.player
  const team = echoTeam(player)
  if (!echoCanStart(team)) return
  const entry = echoCase(echoState(player.getServer()), team, false)
  if (entry === null) return
  for (const milestone of Object.keys(echoTasks)) {
    if (entry.getBoolean(milestone)) echoCredit(player, milestone)
  }
})

BlockEvents.rightClicked(event => {
  if (String(event.getHand()) !== 'MAIN_HAND') return
  const block = event.getBlock()
  const world = String(block.getDimension())
  if (world !== echoWorld && world !== echoDispatchWorld) return
  const id = String(block.getId())
  if (!(id in echoPanels)) return
  const player = event.player
  const state = echoState(player.getServer())
  if (world === echoDispatchWorld) {
    if (id !== 'minecraft:chiseled_bookshelf' || !state.contains('dispatch')) return
    var dispatchDesk = echoPosition(state.getCompound('dispatch'))
    if (block.getX() !== dispatchDesk.getX() || block.getY() !== dispatchDesk.getY() || block.getZ() !== dispatchDesk.getZ()) return
    useEchoDispatch(player, state, echoTeam(player))
    event.cancel()
    return
  }
  const site = state.getCompound('site')
  if (!site.getBoolean('ready')) return
  const spot = echoPosition(site)
  const x = block.getX() - spot.getX(), y = block.getY() - spot.getY(), z = block.getZ() - spot.getZ()
  const panel = echoPanels[id].find(([px, py, pz]) => x === px && y === py && z === pz)
  if (!panel) return
  const action = panel[3]
  const team = echoTeam(player)
  const entry = echoCanStart(team) ? echoCase(state, team, false) : null
  const station = id === 'swa_research:station'
  if (entry === null && !(station && action === 'ship_buckle')) {
    echoTell(player, 'briefing_required')
  } else if (station) {
    var stationResult = String(HothResearch.perform(player, block.getX(), block.getY(), block.getZ(), event.getHand(), action))
    if (entry !== null) {
      if (stationResult === 'historical_echo') echoMark(player, entry, 'signal')
      if (stationResult === 'buckle_echo') entry.putBoolean('kit_clue', true)
    }
  } else {
    useEchoPanel(player, entry, action)
  }
  event.cancel()
})
