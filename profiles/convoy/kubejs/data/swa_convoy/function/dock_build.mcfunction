execute in starwarsmod:tatooine unless loaded -14 139 -43 run return run schedule function swa_convoy:dock_build 2s replace
execute in starwarsmod:tatooine unless loaded 14 140 -12 run return run schedule function swa_convoy:dock_build 2s replace
execute in starwarsmod:tatooine run function swa_convoy:dock_build_pad
execute in starwarsmod:tatooine positioned 0 140 -12 store result storage swa_convoy:mission dock_ready byte 1 run swa_convoy_register_dock
execute in starwarsmod:tatooine run forceload remove -14 -43 14 -12
