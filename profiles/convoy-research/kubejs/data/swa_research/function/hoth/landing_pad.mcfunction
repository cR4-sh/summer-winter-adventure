# Run at the station floor. Work with existing stations that still have their original roof.
execute unless block ~ ~4 ~ minecraft:cyan_terracotta run return 0
fill ~-8 ~5 ~-9 ~8 ~5 ~7 minecraft:smooth_stone keep
fill ~-6 ~5 ~-7 ~-6 ~5 ~6 minecraft:sea_lantern replace minecraft:smooth_stone
fill ~6 ~5 ~-7 ~6 ~5 ~6 minecraft:sea_lantern replace minecraft:smooth_stone

# Ladder on the south edge, outside the approach and touchdown area.
fill ~4 ~1 ~7 ~4 ~5 ~7 minecraft:polished_andesite keep
fill ~4 ~1 ~8 ~4 ~5 ~8 minecraft:ladder[facing=south] keep

setblock ~ ~6 ~-7 unbound:landing_beacon[lit=true]{owner:[I;338237283,776031352,-1847946961,-1045438090],keeper:"SWA Hoth Relay",name:"Пункт связи на Хоте"} keep
execute positioned ~ ~6 ~-7 run return run swa_convoy_register_dock
