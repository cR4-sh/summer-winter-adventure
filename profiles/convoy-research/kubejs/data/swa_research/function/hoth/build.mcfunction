# Execute at the reserved floor origin in starwarsmod:hoth.
# Site bounds: x -12..12, y -2..6, z -10..10.
fill ~-12 ~-2 ~-10 ~12 ~-1 ~10 minecraft:polished_deepslate
fill ~-12 ~ ~-10 ~12 ~ ~10 minecraft:smooth_stone
fill ~-12 ~1 ~-10 ~12 ~6 ~10 minecraft:air

# Reception hall, roofline, and walk-in entrance facing south.
fill ~-5 ~ ~-7 ~5 ~ ~6 minecraft:polished_andesite
fill ~-5 ~1 ~-7 ~5 ~3 ~-7 minecraft:light_gray_concrete
fill ~-5 ~1 ~-7 ~-5 ~3 ~6 minecraft:light_gray_concrete
fill ~5 ~1 ~-7 ~5 ~3 ~6 minecraft:light_gray_concrete
fill ~-5 ~1 ~6 ~5 ~3 ~6 minecraft:light_gray_concrete
fill ~-1 ~1 ~6 ~1 ~3 ~6 minecraft:air
fill ~-4 ~2 ~6 ~-2 ~2 ~6 minecraft:tinted_glass
fill ~2 ~2 ~6 ~4 ~2 ~6 minecraft:tinted_glass
fill ~-5 ~4 ~-7 ~5 ~4 ~6 minecraft:cyan_terracotta
fill ~-5 ~5 ~-7 ~5 ~5 ~-7 minecraft:oxidized_cut_copper
fill ~-5 ~5 ~6 ~5 ~5 ~6 minecraft:oxidized_cut_copper
fill ~-5 ~1 ~-7 ~5 ~1 ~-7 minecraft:polished_deepslate
fill ~-1 ~ ~6 ~1 ~ ~10 minecraft:cyan_concrete
setblock ~-2 ~ ~9 minecraft:sea_lantern
setblock ~2 ~ ~9 minecraft:sea_lantern
setblock ~-1 ~4 ~6 minecraft:sea_lantern
setblock ~1 ~4 ~6 minecraft:sea_lantern
setblock ~0 ~3 ~6 minecraft:spruce_hanging_sign[rotation=0,attached=true]{is_waxed:1b,front_text:{messages:['{"text":"ПУНКТ"}','{"text":"СВЯЗИ"}','{"text":"ХОТ"}','{"text":""}'],color:"white",has_glowing_text:1b}}

# Shelter wings share their layout. Both hatches are physically open.
fill ~-11 ~ ~-3 ~-5 ~ ~3 minecraft:polished_andesite
fill ~5 ~ ~-3 ~11 ~ ~3 minecraft:polished_andesite
fill ~-11 ~1 ~-3 ~-5 ~3 ~-3 minecraft:light_gray_concrete
fill ~-11 ~1 ~3 ~-5 ~3 ~3 minecraft:light_gray_concrete
fill ~5 ~1 ~-3 ~11 ~3 ~-3 minecraft:light_gray_concrete
fill ~5 ~1 ~3 ~11 ~3 ~3 minecraft:light_gray_concrete
fill ~-11 ~1 ~-3 ~-11 ~3 ~3 minecraft:light_gray_concrete
fill ~11 ~1 ~-3 ~11 ~3 ~3 minecraft:light_gray_concrete
fill ~-11 ~4 ~-3 ~-5 ~4 ~3 minecraft:light_gray_concrete
fill ~5 ~4 ~-3 ~11 ~4 ~3 minecraft:light_gray_concrete
fill ~-11 ~5 ~-3 ~-5 ~5 ~-3 minecraft:polished_deepslate
fill ~5 ~5 ~-3 ~11 ~5 ~-3 minecraft:polished_deepslate
fill ~-11 ~5 ~3 ~-5 ~5 ~3 minecraft:polished_deepslate
fill ~5 ~5 ~3 ~11 ~5 ~3 minecraft:polished_deepslate
fill ~-5 ~1 ~-1 ~-5 ~2 ~1 minecraft:air
fill ~5 ~1 ~-1 ~5 ~2 ~1 minecraft:air
fill ~-5 ~3 ~-1 ~-5 ~3 ~1 minecraft:cyan_terracotta
fill ~5 ~3 ~-1 ~5 ~3 ~1 minecraft:cyan_terracotta
fill ~-10 ~ ~ ~-4 ~ ~ minecraft:blue_concrete
fill ~4 ~ ~ ~10 ~ ~ minecraft:yellow_concrete
setblock ~-5 ~2 ~0 minecraft:spruce_hanging_sign[rotation=4,attached=true]{is_waxed:1b,front_text:{messages:['{"text":"ЗАПАДНОЕ"}','{"text":"УКРЫТИЕ"}','{"text":""}','{"text":""}'],color:"white",has_glowing_text:1b},back_text:{messages:['{"text":"ЗАПАДНОЕ"}','{"text":"УКРЫТИЕ"}','{"text":""}','{"text":""}'],color:"white",has_glowing_text:1b}}
setblock ~5 ~2 ~0 minecraft:spruce_hanging_sign[rotation=4,attached=true]{is_waxed:1b,front_text:{messages:['{"text":"ВОСТОЧНОЕ"}','{"text":"УКРЫТИЕ"}','{"text":""}','{"text":""}'],color:"white",has_glowing_text:1b},back_text:{messages:['{"text":"ВОСТОЧНОЕ"}','{"text":"УКРЫТИЕ"}','{"text":""}','{"text":""}'],color:"white",has_glowing_text:1b}}
setblock ~-10 ~4 ~0 minecraft:sea_lantern
setblock ~10 ~4 ~0 minecraft:sea_lantern
setblock ~-11 ~2 ~0 minecraft:blue_stained_glass
setblock ~11 ~2 ~0 minecraft:yellow_stained_glass
setblock ~-9 ~1 ~0 minecraft:lodestone
setblock ~9 ~1 ~0 minecraft:lodestone
setblock ~-9 ~2 ~0 minecraft:spruce_sign[rotation=12]{is_waxed:1b,front_text:{messages:['{"text":"ЗАПАД"}','{"text":"Осмотреть"}','{"text":"укрытие"}','{"text":""}'],color:"white",has_glowing_text:1b}}
setblock ~9 ~2 ~0 minecraft:spruce_sign[rotation=4]{is_waxed:1b,front_text:{messages:['{"text":"ВОСТОК"}','{"text":"Осмотреть"}','{"text":"укрытие"}','{"text":""}'],color:"white",has_glowing_text:1b}}
fill ~-10 ~1 ~-2 ~-9 ~1 ~-2 minecraft:red_wool
fill ~9 ~1 ~-2 ~10 ~1 ~-2 minecraft:red_wool
setblock ~-10 ~2 ~-2 minecraft:white_carpet
setblock ~10 ~2 ~-2 minecraft:white_carpet
setblock ~-10 ~1 ~2 minecraft:cauldron
setblock ~10 ~1 ~2 minecraft:cauldron

# Emergency bulkhead and common table. The two glove positions use the same station model.
fill ~-1 ~1 ~-7 ~1 ~3 ~-7 minecraft:iron_block
setblock ~0 ~2 ~-7 minecraft:iron_trapdoor[facing=south,half=top,open=true]
setblock ~-2 ~3 ~-7 minecraft:ochre_froglight
setblock ~2 ~3 ~-7 minecraft:ochre_froglight
setblock ~0 ~3 ~-6 minecraft:spruce_hanging_sign[rotation=0,attached=true]{is_waxed:1b,front_text:{messages:['{"text":"АВАРИЙНЫЙ"}','{"text":"ШЛЮЗ"}','{"text":""}','{"text":""}'],color:"white",has_glowing_text:1b}}
fill ~-3 ~1 ~-6 ~3 ~1 ~-6 minecraft:dark_oak_planks
setblock ~-3 ~1 ~-5 minecraft:smooth_quartz
setblock ~3 ~1 ~-5 minecraft:smooth_quartz
setblock ~-2 ~1 ~-5 swa_research:station[action=control_glove]
setblock ~0 ~1 ~-5 swa_research:station[action=historical_glove]
setblock ~2 ~1 ~-5 swa_research:station[action=comparison_token]
setblock ~-2 ~2 ~-6 minecraft:spruce_sign[rotation=0]{is_waxed:1b,front_text:{messages:['{"text":"ПЕРЧАТКА"}','{"text":"Запасная"}','{"text":"Со склада"}','{"text":""}'],color:"white",has_glowing_text:1b}}
setblock ~0 ~2 ~-6 minecraft:spruce_sign[rotation=0]{is_waxed:1b,front_text:{messages:['{"text":"ПЕРЧАТКА"}','{"text":"Радиста"}','{"text":"Из шлюза"}','{"text":""}'],color:"white",has_glowing_text:1b}}
setblock ~2 ~2 ~-6 minecraft:spruce_sign[rotation=0]{is_waxed:1b,front_text:{messages:['{"text":"ЖЕТОН"}','{"text":"Связистки"}','{"text":"Металл"}','{"text":""}'],color:"white",has_glowing_text:1b}}

# Personal lesson and both predictions are independent of the team investigation.
setblock ~-3 ~1 ~-1 swa_research:station[action=enroll]
setblock ~-3 ~2 ~-1 minecraft:spruce_sign[rotation=12]{is_waxed:1b,front_text:{messages:['{"text":"АРХИВИСТ"}','{"text":"Добровольный"}','{"text":"урок и журнал"}','{"text":""}'],color:"white",has_glowing_text:1b}}
setblock ~-3 ~1 ~-3 swa_research:station[action=predict_material]
setblock ~3 ~1 ~-3 swa_research:station[action=predict_personal_history]
setblock ~-3 ~2 ~-3 minecraft:spruce_sign[rotation=12]{is_waxed:1b,front_text:{messages:['{"text":"ПРОГНОЗ"}','{"text":"Важен материал"}','{"text":"Жетон пуст"}','{"text":""}'],color:"white",has_glowing_text:1b}}
setblock ~3 ~2 ~-3 minecraft:spruce_sign[rotation=4]{is_waxed:1b,front_text:{messages:['{"text":"ПРОГНОЗ"}','{"text":"Важно событие"}','{"text":"У жетона след"}','{"text":""}'],color:"white",has_glowing_text:1b}}
setblock ~0 ~4 ~-3 minecraft:sea_lantern
setblock ~0 ~3 ~-3 minecraft:spruce_hanging_sign[rotation=0,attached=true]{is_waxed:1b,front_text:{messages:['{"text":"ГИПОТЕЗА"}','{"text":"Материал"}','{"text":"или"}','{"text":"событие?"}'],color:"white",has_glowing_text:1b}}

# South-facing engineering consoles. The team logic supplies live circuit readouts.
fill ~-3 ~ ~4 ~3 ~ ~4 minecraft:cyan_terracotta
setblock ~-2 ~1 ~4 minecraft:chiseled_bookshelf[facing=south]
setblock ~0 ~1 ~4 minecraft:copper_bulb[lit=false,powered=false]
setblock ~2 ~1 ~4 minecraft:cartography_table
setblock ~-2 ~2 ~4 minecraft:spruce_sign[rotation=0]{is_waxed:1b,front_text:{messages:['{"text":"РЕГИСТРАТОР"}','{"text":"Воспроизвести"}','{"text":"запись"}','{"text":""}'],color:"white",has_glowing_text:1b}}
setblock ~0 ~2 ~4 minecraft:spruce_sign[rotation=0]{is_waxed:1b,front_text:{messages:['{"text":"ПИТАНИЕ"}','{"text":"Включить"}','{"text":"регистратор"}','{"text":""}'],color:"white",has_glowing_text:1b}}
setblock ~2 ~2 ~4 minecraft:spruce_sign[rotation=0]{is_waxed:1b,front_text:{messages:['{"text":"ЭВАКУАЦИЯ"}','{"text":"Схема линий"}','{"text":"к укрытиям"}','{"text":""}'],color:"white",has_glowing_text:1b}}
setblock ~-3 ~4 ~4 minecraft:sea_lantern
setblock ~3 ~4 ~4 minecraft:sea_lantern

# Exterior loading ramp and repeatable second object for the return trip.
fill ~4 ~ ~7 ~10 ~ ~10 minecraft:polished_deepslate
fill ~6 ~1 ~7 ~10 ~1 ~7 minecraft:polished_deepslate_slab[type=bottom,waterlogged=false]
setblock ~6 ~ ~9 minecraft:white_concrete
setblock ~8 ~ ~9 minecraft:white_concrete
setblock ~10 ~ ~9 minecraft:white_concrete
setblock ~5 ~1 ~8 swa_research:station[action=ship_buckle]
setblock ~5 ~2 ~8 minecraft:spruce_sign[rotation=0]{is_waxed:1b,front_text:{messages:['{"text":"ПРЯЖКА"}','{"text":"Старый ящик"}','{"text":"у рампы"}','{"text":""}'],color:"white",has_glowing_text:1b}}
setblock ~7 ~1 ~9 minecraft:smooth_stone
setblock ~7 ~2 ~9 minecraft:spruce_sign[rotation=0]{is_waxed:1b,front_text:{messages:['{"text":"ОСМОТР РАМПЫ"}','{"text":"Проверить"}','{"text":"белые риски"}','{"text":"на настиле"}'],color:"white",has_glowing_text:1b}}
setblock ~10 ~1 ~8 minecraft:chiseled_copper
setblock ~10 ~2 ~8 minecraft:spruce_sign[rotation=0]{is_waxed:1b,front_text:{messages:['{"text":"СЕРВИСНЫЙ"}','{"text":"ОТСЕК"}','{"text":""}','{"text":""}'],color:"white",has_glowing_text:1b}}
setblock ~11 ~ ~9 minecraft:sea_lantern

return 1
