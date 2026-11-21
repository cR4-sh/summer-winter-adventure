execute unless data storage swa_convoy:mission {station_ready:1b} run function swa_convoy:prepare_chunks
function swa_convoy:relay_prepare
function swa_convoy:dock_prepare
execute unless data storage swa_convoy:mission {death_star_dock_ready:1b} in starwarsmod:death_star run forceload add 644 575 660 575
execute unless data storage swa_convoy:mission {death_star_dock_ready:1b} run schedule function swa_convoy:death_star_dock_build 2s replace
schedule function swa_convoy:check_in 1s replace
