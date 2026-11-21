execute if entity @s[tag=swa_convoy_paid] run return 0
tag @s add swa_convoy_paid
give @s minecraft:emerald 6
tellraw @s {"text":"Конвой обнаружен. Новая Республика перечислила награду.","color":"green"}
