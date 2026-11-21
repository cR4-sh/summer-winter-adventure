execute if data storage swa_convoy:mission {relay_ready:1b} run return 0
execute in starwarsmod:tatooine run forceload add -11 12
execute in starwarsmod:tatooine run forceload add 270 -14 306 14
execute in starwarsmod:tatooine run forceload add 420 -14 444 14
schedule function swa_convoy:relay_build 2s replace
