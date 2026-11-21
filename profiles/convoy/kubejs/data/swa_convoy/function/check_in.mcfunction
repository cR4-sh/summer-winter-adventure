execute in starwarsmod:tatooine as @a[x=-14,y=140,z=-14,dx=28,dy=5,dz=28,tag=!swa_convoy_reported,gamemode=!spectator] at @s run function swa_convoy:report
execute in starwarsmod:tatooine as @e[type=starwarsmod:falcon,tag=swa_service_ship,tag=!swa_convoy_relabelled] run function swa_convoy:relabel_legacy_falcon
schedule function swa_convoy:check_in 1s replace
