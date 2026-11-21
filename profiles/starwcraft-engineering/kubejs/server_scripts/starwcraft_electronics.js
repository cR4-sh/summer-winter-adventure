// Keep the stock beacon ID so all StarWCraft ship recipes still use it.
ServerEvents.recipes(event => {
  event.remove({ id: 'starwarsmod:hyperspace_beacon' })
  event.shaped('starwarsmod:hyperspace_beacon', [
    'ED ',
    'DKD',
    ' D '
  ], {
    E: 'hbm:circuit_basic',
    D: 'starwarsmod:durasteel_ingot',
    K: 'starwarsmod:kyber_crystal'
  }).id('starwarsmod:hyperspace_beacon')
})
