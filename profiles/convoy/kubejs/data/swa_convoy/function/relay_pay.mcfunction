execute if entity @s[tag=swa_relay_paid] run return 0
tag @s add swa_relay_paid
give @s minecraft:emerald 8
tellraw @s {"text":"Гражданский транспорт забрал медгруз. Диспетчер сменил ваш позывной и отложил для вас копию запроса с Хота.","color":"aqua"}
