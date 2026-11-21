execute if entity @s[tag=swa_convoy_reported] run return 0
execute unless dimension starwarsmod:tatooine run return 0
execute positioned -14 140 -14 unless entity @s[dx=28,dy=5,dz=28] run return 0
execute unless data storage swa_convoy:mission {station_ready:1b} run return 0
give @s starwarsmod:blaster_pistol
give @s starwarsmod:power_cell 24
give @s minecraft:shield
give @s minecraft:iron_chestplate
give @s minecraft:cooked_beef 16
give @s minecraft:water_bucket
tag @s add swa_convoy_reported
tag @s remove swa_convoy_waiting
tellraw @s {"text":"На востоке пропал транспорт. Ищите оранжевую площадку на X 144, Y 140, Z 0.","color":"aqua"}
