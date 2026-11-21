execute unless entity @s[type=minecraft:player,tag=!swa_convoy_albatross_issued] run return 0
give @s unbound:ship_albatross
tag @s add swa_convoy_albatross_issued
