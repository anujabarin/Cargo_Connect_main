<?php

namespace App\Scenarios;

use App\Models\Agent;
use Illuminate\Support\Facades\DB;

class PackageDelayScenario implements TrafficScenario
{
    public function run(): void
    {
        $driverWaitingAreaCode = 'driver-waiting-area';
        // Normal route agents = green
        Agent::where('code', '!=', $driverWaitingAreaCode)->update(['state' => 'G']);

        // Add reroute waypoint (pretend alternate street)
        DB::table('agents')->where([
            'code' => $driverWaitingAreaCode,
        ])->update(
            [
                'state' => 'Y',
            ]
        );
    }
}
