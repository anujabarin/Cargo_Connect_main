<?php

namespace App\Scenarios;

use App\Models\Agent;
use Illuminate\Support\Facades\DB;

class AccidentAtCoitScenario implements TrafficScenario
{
    public function run(): void
    {
        $accidentAgentCode = 'Agent3';
        Agent::where('code', $accidentAgentCode)->update(['state' => 'R']);
        // All others green
        Agent::where('code', '!=', $accidentAgentCode)->update(['state' => 'G']);

        // // Add reroute waypoint (pretend alternate street)
        // DB::table('agents')->where([
        //     'code' => 'reroute-via-arapaho',
        // ])->update(
        //     [
        //         'state' => 'G',
        //     ]
        // );
    }
}
