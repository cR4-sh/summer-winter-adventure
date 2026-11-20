ServerEvents.recipes(event => {
    const boards = 'kubejs:lunar_circuit_board';
    const pcb = 'hbm:circuit_pcb';

    event.recipes.hbm.assembly_machine('16x ' + boards, [
        '16x ' + pcb, 'unbound:moonstone'
    ], 200, 400).id('kubejs:tech/lunar_circuit_board');
});
