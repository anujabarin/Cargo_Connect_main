<?php

namespace App\Console\Commands;

use App\DataTransferObjects\NotificationDTO;
use App\Models\Agent;
use App\Models\EventLog;
use App\Models\NotificationLog;
use Illuminate\Console\Command;

class SimulateTraffic extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'app:simulate-traffic';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Simulate traffic changes';

    /**
     * Execute the console command.
     */
    public function handle(): void
    {
        $this->info('Starting traffic simulation... (press Ctrl+C to stop)');

        $states = ['R', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'Y'];

        while (true) {
            $agents = Agent::inRandomOrder()->limit(2)->get();

            foreach ($agents as $agent) {
                $oldState = $agent->state;
                $newState = collect($states)->reject(fn ($s) => $s === $oldState)->random();
                $agent->update(['state' => $newState]);

                EventLog::create([
                    'agent_id' => $agent->id,
                    'event_type' => 'STATE_CHANGE',
                    'metadata' => json_encode(['from' => $oldState, 'to' => $newState]),
                ]);

                $message = "Agent {$agent->name} changed state from {$oldState} to {$newState}";

                NotificationLog::create([
                    'agent_id' => $agent->id,
                    'type' => 'STATUS_CHANGE',
                    'message' => $message,
                ]);

                $dto = new NotificationDTO(
                    agent_id: $agent->id,
                    agent_type: $agent->type,
                    notification_type: 'STATUS_CHANGE',
                    message: $message,
                );

                // Normally you'd broadcast here or push to frontend
                // $this->info(json_encode($dto->toArray()));
                $this->info("Simulated notification: {$message}");
            }

            sleep(1); // Simulate a delay between traffic changes
        }
    }
}
