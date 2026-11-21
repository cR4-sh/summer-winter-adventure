execute if data storage swa_convoy:mission {station_ready:1b} run return 0
execute in starwarsmod:tatooine unless loaded -14 140 -14 run return run schedule function swa_convoy:build 2s replace
execute in starwarsmod:tatooine unless loaded 156 140 14 run return run schedule function swa_convoy:build 2s replace
execute in starwarsmod:tatooine run function swa_convoy:build_decks
data modify storage swa_convoy:mission station_ready set value 1b
execute in starwarsmod:tatooine run forceload remove -14 -14 14 14
execute in starwarsmod:tatooine run forceload remove 132 -14 156 14
