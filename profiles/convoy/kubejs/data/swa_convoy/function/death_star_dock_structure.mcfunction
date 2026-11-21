# Match the original imported hangar before modifying its roof.
execute unless block 660 107 575 minecraft:cyan_terracotta run return 0
execute unless block 645 107 575 minecraft:cyan_terracotta run return 0
execute unless block 645 79 575 minecraft:gray_concrete run return 0
execute unless block 644 24 575 minecraft:black_carpet run return 0

# The shaft passes through two roof planes beside the hangar spawn.
fill 644 79 575 644 79 575 minecraft:air replace minecraft:gray_concrete
fill 644 107 575 644 107 575 minecraft:air replace minecraft:cyan_terracotta
fill 645 25 575 645 109 575 minecraft:gray_concrete keep
fill 644 25 575 644 109 575 minecraft:ladder[facing=west] keep
setblock 644 108 574 minecraft:iron_bars keep
setblock 644 108 576 minecraft:iron_bars keep
setblock 645 110 575 minecraft:sea_lantern keep

setblock 660 108 575 unbound:landing_beacon[lit=true]{owner:[I;338237283,776031352,-1847946961,-1045438090],keeper:"Death Star",name:"Ангар Звезды Смерти"} keep
execute positioned 660 108 575 store result storage swa_convoy:mission death_star_dock_ready byte 1 run swa_convoy_register_dock
