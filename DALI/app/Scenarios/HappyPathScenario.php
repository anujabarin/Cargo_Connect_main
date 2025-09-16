<?php

namespace App\Scenarios;

use App\Models\Agent;

class HappyPathScenario implements TrafficScenario
{
    public function run(): void
    {
        Agent::query()->update(['state' => 'G']);
    }
}
