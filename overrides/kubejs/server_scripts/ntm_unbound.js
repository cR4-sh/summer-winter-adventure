ServerEvents.recipes(event => {
    const ntm = event.recipes.hbm;
    const kerosene = amount => [{ type: 'hbm:kerosene', amount: amount }];

    ntm.assembly_machine('kubejs:sealed_cabin_assembly', [
        '4x hbm:plate_titanium', '2x hbm:ingot_bakelite', 'hbm:circuit_basic'
    ], 240, 200).inputFluids(kerosene(1000)).id('kubejs:tech/sealed_cabin_assembly');

    ntm.assembly_machine('kubejs:reinforced_porthole', [
        'unbound:optical_glass', 'hbm:plate_titanium', 'hbm:ingot_bakelite'
    ], 120, 200).id('kubejs:tech/reinforced_porthole');

    ntm.assembly_machine('kubejs:shipyard_avionics', [
        'unbound:avionics', 'hbm:circuit_advanced', 'hbm:motor', '2x hbm:plate_dura_steel'
    ], 320, 400).id('kubejs:tech/shipyard_avionics');

    ntm.assembly_machine('kubejs:shielded_navigation_core', [
        'unbound:navigation_crystal', '2x hbm:plate_lead', 'hbm:circuit_advanced'
    ], 300, 400).id('kubejs:tech/shielded_navigation_core');

    ntm.assembly_machine('kubejs:interstellar_controller', [
        'unbound:threshold_key', 'hbm:circuit_quantum', '2x hbm:ingot_desh', '2x hbm:plate_dura_steel'
    ], 480, 800).id('kubejs:tech/interstellar_controller');

    ntm.assembly_machine('unbound:fuel_canister', ['unbound:empty_canister'], 80, 100)
        .inputFluids(kerosene(1000)).id('kubejs:tech/fuel_canister_from_kerosene');

    // Match the stock recipes by ID so the journal and recipe book keep their references.
    const replacements = [
        ['lunar_rocket', 'minecraft:leather', 'sealed_cabin_assembly'],
        ['rift_shuttle', 'unbound:optical_glass', 'reinforced_porthole'],
        ['diving_bell', 'unbound:optical_glass', 'reinforced_porthole'],
        ['deep_space_rocket', 'unbound:navigation_crystal', 'shielded_navigation_core'],
        ['shipyard_console_space', 'unbound:avionics', 'shipyard_avionics'],
        ['shipyard_console_rift', 'unbound:avionics', 'shipyard_avionics'],
        ['shipyard_console_vent', 'unbound:avionics', 'shipyard_avionics'],
        ['ship_module_star_drive_1', 'unbound:threshold_key', 'interstellar_controller']
    ];

    replacements.forEach(([recipe, original, part]) => {
        const filter = { id: 'unbound:' + recipe, input: original };
        if (event.countRecipes(filter) !== 1) {
            throw new Error('Expected Unbound recipe with vanilla input: ' + recipe);
        }
        event.replaceInput(filter, original, 'kubejs:' + part);
    });
});
