execute if data storage swa_convoy:mission {dock_ready:1b} run return 0
execute unless data storage swa_convoy:mission {station_ready:1b} run return run schedule function swa_convoy:dock_prepare 2s replace
execute in starwarsmod:tatooine run forceload add -14 -43 14 -12
schedule function swa_convoy:dock_build 2s replace
