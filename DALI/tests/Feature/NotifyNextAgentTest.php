<?php

namespace Tests\Feature;

use App\Models\Agent;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class NotifyNextAgentTest extends TestCase
{
    use RefreshDatabase;

    /**
     * A basic feature test example.
     */
    public function test_example(): void
    {
        $agent = Agent::updateOrCreate([
            'code' => 'Agent1',
        ], [
            'name' => 'AGENT - 1',
            'type' => 'checkpoint',
            'latitude' => 12.3456789,
            'longitude' => 98.7654321,
            'state' => 'G',
            // 'state' => 'active',
        ]);

        $response = $this->postJson('/driver/location', [
            'next_agent_code' => 'Agent1',
        ]);

        // dd($response->json());

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'ok',
                'near' => true,
                'agent_code' => 'Agent1',
                'agent_type' => 'checkpoint',
                'agent_state' => 'G',
                'message' => 'All systems normal. ✅',
            ]);
        // ->assertJsonStructure([
        //     'status',
        //     'near',
        //     'agent_code',
        //     'agent_type',
        //     'agent_state',
        //     'message',
        // ]);

    }
}
