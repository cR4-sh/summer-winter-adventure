const ConvoyLandingBeacons = Java.loadClass('me.moltea.unbound.space.beacon.LandingBeacons')
const ConvoyLandingBeacon = Java.loadClass('me.moltea.unbound.space.beacon.LandingBeacons$Beacon')
const ConvoyLandingBeaconBlockEntity = Java.loadClass('me.moltea.unbound.space.beacon.LandingBeaconBlockEntity')

ServerEvents.loaded(event => {
  event.server.runCommandSilent('function swa_convoy:prepare')
})

ServerEvents.commandRegistry(event => {
  event.register(event.commands.literal('swa_convoy_register_dock')
    .requires(source => source.hasPermission(2))
    .executes(context => {
      var source = context.getSource()
      var level = source.getLevel()
      var pos = ConvoyBlockPos.containing(source.getPosition())
      var beacon = level.getBlockEntity(pos)
      if (!(beacon instanceof ConvoyLandingBeaconBlockEntity) || beacon.owner() === null) return 0

      ConvoyLandingBeacons.of(level).put(new ConvoyLandingBeacon(pos, beacon.owner(), beacon.keeper(), beacon.nameText()))
      return 1
    }))
})
