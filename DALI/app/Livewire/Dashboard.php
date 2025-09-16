<?php

namespace App\Livewire;

use App\Models\Agent;
use Illuminate\Support\Facades\Artisan;
use Livewire\Component;

class Dashboard extends Component
{
    public $agents;

    public string $currentScenario = ''; // Default scenario

    public function mount()
    {
        // Fetch all agents at the beginning
        $this->agents = Agent::all();

        // // Initially dispatch the graph with the default scenario
        // $this->dispatch('renderGraph', $this->getGraphData('happy')); // Default "happy" scenario
    }

    public function render()
    {
        return view('livewire.dashboard');
    }

    public function setScenario(string $scenario)
    {
        // $this->currentScenario = $scenario;

        // Run the Artisan command to simulate the scenario
        Artisan::call("app:simulate-scenario {$scenario}");

        return $this->skipRender();

        // // Get the graph data after the scenario is set
        // $graphData = $this->getGraphData($scenario);

        // // Dispatch an event to update the graph on the front-end
        // $this->dispatch('renderGraph', $graphData);
    }

    // public function getGraphData($scenario): array
    // {
    //     // Get agent codes from the scenario configuration
    //     $agentCodes = config("scenarios.$scenario", []);
    //     $allAgents = Agent::all()->keyBy('code'); // Key the agents by their code for easy lookup

    //     // Prepare the nodes and edges for the graph
    //     $nodes = [];
    //     $edges = [];

    //     // Loop through all agents to prepare the nodes
    //     foreach ($allAgents as $agent) {
    //         // Set the color of the agent based on whether it's in the scenario path or not
    //         $color = in_array($agent->code, $agentCodes) ? '#FF0000' : '#CCCCCC'; // Default color for agents not in the scenario

    //         // Add the agent as a node
    //         $nodes[] = [
    //             'id' => $agent->code,
    //             'label' => $agent->name,
    //             'color' => $color,  // Highlight agents in the scenario
    //         ];
    //     }

    //     // Generate edges based on the sequence of agent codes in the active scenario
    //     for ($i = 0; $i < count($agentCodes) - 1; $i++) {
    //         $from = $agentCodes[$i];
    //         $to = $agentCodes[$i + 1];
    //         $edges[] = [
    //             'from' => $from,
    //             'to' => $to,
    //         ];
    //     }

    //     return [
    //         'nodes' => $nodes,
    //         'edges' => $edges,
    //     ];
    // }
}
