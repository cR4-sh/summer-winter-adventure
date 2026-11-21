execute if data storage swa_convoy:mission {relay_ready:1b} run return 0
execute unless data storage swa_convoy:mission {station_ready:1b} run return run schedule function swa_convoy:relay_build 2s replace
execute in starwarsmod:tatooine unless loaded -11 140 12 run return run function swa_convoy:relay_prepare
execute in starwarsmod:tatooine unless loaded 270 139 -14 run return run schedule function swa_convoy:relay_build 2s replace
execute in starwarsmod:tatooine unless loaded 306 145 14 run return run schedule function swa_convoy:relay_build 2s replace
execute in starwarsmod:tatooine unless loaded 420 139 -14 run return run schedule function swa_convoy:relay_build 2s replace
execute in starwarsmod:tatooine unless loaded 444 144 14 run return run schedule function swa_convoy:relay_build 2s replace

execute in starwarsmod:tatooine run function swa_convoy:relay_build_sites
data modify storage swa_convoy:mission relay_ready set value 1b
execute in starwarsmod:tatooine run forceload remove -11 12
execute in starwarsmod:tatooine run forceload remove 270 -14 306 14
execute in starwarsmod:tatooine run forceload remove 420 -14 444 14
