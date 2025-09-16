<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;

class SimulateScenario extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'app:simulate-scenario {name}';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Command description';

    /**
     * Execute the console command.
     */
    public function handle()
    {
        $scenarioMap = [
            'happy' => \App\Scenarios\HappyPathScenario::class,
            'edge-accident' => \App\Scenarios\AccidentAtCoitScenario::class,
            'edge-delay' => \App\Scenarios\PackageDelayScenario::class,
            'edge-parking' => \App\Scenarios\NoParkingScenario::class,
        ];

        $scenario = $scenarioMap[$this->argument('name')] ?? null;

        if (! $scenario || ! class_exists($scenario)) {
            $this->error('Invalid scenario.');

            return;
        }

        app($scenario)->run();

        $this->info("Scenario '{$this->argument('name')}' executed.");
    }
}
