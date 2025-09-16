<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Agent;
use App\Models\NotificationLog;
use App\Support\NotificationMessageResolver;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Log;

class AgentController extends Controller
{
    public function index()
    {
        return Agent::all();
    }

    public function notifications(Agent $agent)
    {
        return NotificationLog::where('agent_id', $agent->id)->latest()->get();
    }

    public function notifyNextAgents(Request $request)
    {
        // $lat = $request->input('latitude');
        // $lng = $request->input('longitude');

        // if (! $agentCode || ! $lat || ! $lng) {
        //     return response()->json(['message' => 'Lat/Lng not sent'], 422);
        // }

        $agentCode = $request->input('next_agent_code');
        $allTxt = json_encode($request->all());
        // Log::info("Agent code: {$allTxt}");
        $agent = Agent::where('code', $agentCode)->first();

        // Get random agent
        if (! $agent) {
            Log::info("Agent code not found: {$agentCode}");

            return response()->json(['message' => 'Invalid agent code'], 404);
        }

        $message = NotificationMessageResolver::resolve($agent);
        Log::info("Agent code found: {$agentCode} ==> Message: {$message}");

        return response()->json([
            'status' => 'ok',
            'near' => true,
            'agent_code' => $agent->code,
            'agent_type' => $agent->type,
            'agent_state' => $agent->state,
            'message' => $message,
        ]);
    }

    public function getAgentStates()
    {
        $agents = Agent::all();

        $agentStates = [];

        foreach ($agents as $agent) {
            $agentStates[] = [
                'code' => $agent->code,
                'name' => $agent->name,
                'type' => $agent->type,
                'state' => $agent->state,
                'latitude' => $agent->latitude,
                'longitude' => $agent->longitude,
            ];
        }

        return response()->json([
            'status' => 'ok',
            'agents' => $agentStates,
        ])->setEncodingOptions(JSON_UNESCAPED_SLASHES | JSON_PRETTY_PRINT);
    }

    private function isWithinRadius($lat1, $lng1, $lat2, $lng2, float $radius)
    {
        $earthRadius = 6371000; // meters

        $dLat = deg2rad($lat2 - $lat1);
        $dLng = deg2rad($lng2 - $lng1);

        $a = sin($dLat / 2) * sin($dLat / 2) +
             cos(deg2rad($lat1)) * cos(deg2rad($lat2)) *
             sin($dLng / 2) * sin($dLng / 2);

        $c = 2 * atan2(sqrt($a), sqrt(1 - $a));
        $distance = $earthRadius * $c;

        return $distance <= $radius;
    }
}
