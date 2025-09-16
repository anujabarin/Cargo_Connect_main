<?php

namespace Database\Seeders;

use App\Models\Agent;
use Illuminate\Database\Seeder;

class AgentSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        $checkpointNames = collect([
            'Union Station Checkpoint', 'Southern Logistics Entry',
            'Amazon Dock 3', 'BNSF Rail Access', 'FedEx Inbound Yard',
            'UPS Ground Checkpoint', 'DHL Express Hub', 'Port of Houston Gate 1',
            'Dallas Love Field Checkpoint', 'Southern Logistics Exit',
            'Amazon Dock 1', 'BNSF Rail Exit', 'FedEx Outbound Yard',
        ]);

        $intersectionNames = collect([
            'Maple & Cedar', 'Park Ln & Greenville', 'Broadway & Wall St',
            'Sunset Blvd & Vine', 'Spring Valley & Coit',
            'Main St & Elm', 'Ross Ave & Harwood', 'Mockingbird & Central',
            'Jefferson & Zang', 'Belt Line & Preston', 'Commerce & Harwood',
            'Lemmon & Oak Lawn', 'Mockingbird & Central', 'Jefferson & Zang',
        ]);
        $trafficLightNames = collect([
            'Elm & Main Light', 'Fitzhugh & Ross Signal', 'MLK Blvd & Malcolm X',
            'Commerce & Harwood Light', 'Lemmon & Oak Lawn Signal',
            'Mockingbird & Central', 'Jefferson & Zang Signal',
            'Belt Line & Preston Light',
            'Commerce & Harwood Signal', 'Lemmon & Oak Lawn Light',
            'Mockingbird & Central Signal', 'Jefferson & Zang Light',
            'Belt Line & Preston Signal', 'Commerce & Harwood Light',
        ]);
        $highwayPointNames = collect([
            'Exit 45A - I-35W', 'US-75 @ Knox St', 'I-10 & La Cienega',
            'Exit 14 - NJ Turnpike', 'I-635 & Marsh Ln',
            'I-20 & I-45', 'I-30 & I-35E', 'I-45 & I-10',
            'I-20 & I-35W', 'I-45 & I-20', 'I-30 & I-45',
            'I-35E & I-20', 'I-10 & I-45', 'I-30 & I-20',
        ]);

        // dd($checkpointNames->count(), $intersectionNames->count(), $trafficLightNames->count(), $highwayPointNames->count());

        // private val startLatLng = LatLng(32.984774220294895, -96.74775350068155)

        Agent::updateOrCreate([
            'code' => 'Agent1',
        ], [
            'name' => 'UT Dallas Checkpoint',
            'type' => 'checkpoint',
            'state' => 'G',
            // 'state' => collect(['R', 'G', 'B', 'Y'])->random(),
            'latitude' => 32.984774220294895,
            'longitude' => -96.74775350068155,
        ]);

        $i = 2;
        $count = 49;
        while ($i <= $count) {

            $type = collect(['checkpoint', 'intersection', 'traffic_light', 'highway_point'])->random();

            $name = match ($type) {
                'checkpoint' => $checkpointNames->random(),
                'intersection' => $intersectionNames->random(),
                'traffic_light' => $trafficLightNames->random(),
                'highway_point' => $highwayPointNames->random(),
                default => "Agent $i"
            };

            Agent::updateOrCreate([
                'code' => "Agent{$i}",
            ], [
                'name' => $name,
                'type' => $type,
                'state' => 'G',
                // 'state' => collect(['R', 'G', 'B', 'Y'])->random(),
                'latitude' => 32.9000 + rand(-100, 100) / 1000,
                'longitude' => -96.8000 + rand(-100, 100) / 1000,
            ]);
            $i++;
        }

        Agent::updateOrCreate([
            'code' => 'Agent50',
        ], [
            'name' => 'DFW Airport Checkpoint',
            'type' => 'checkpoint',
            'state' => 'G',
            // 'state' => collect(['R', 'G', 'B', 'Y'])->random(),
            'latitude' => 32.88981687057822,
            'longitude' => -97.03790148285134,
        ]);
    }
}
