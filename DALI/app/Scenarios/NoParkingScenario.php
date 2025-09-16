<?php

namespace App\Scenarios;

use App\Models\Agent;
use Illuminate\Support\Facades\DB;

class NoParkingScenario implements TrafficScenario
{
    public function run(): void
    {
        $dfwAirportAgentCode = 'dfw-airport-terminal-a';
        Agent::where('code', $dfwAirportAgentCode)->update(['state' => 'R']);

        Agent::where('code', '!=', $dfwAirportAgentCode)->update(['state' => 'G']);

        // Add reroute waypoint (pretend alternate street)
        DB::table('agents')->where([
            'code' => 'alternate-parking',
        ])->update(
            [
                'state' => 'Y',
            ]
        );
    }
}
