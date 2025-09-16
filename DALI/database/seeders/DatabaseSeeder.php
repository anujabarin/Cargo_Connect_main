<?php

namespace Database\Seeders;

use App\Models\User;
// use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;

class DatabaseSeeder extends Seeder
{
    /**
     * Seed the application's database.
     */
    public function run(): void
    {
        // User::factory(10)->create();

        $email = 'test@example.com';
        if (! User::where('email', $email)->exists()) {
            User::factory()->create([
                'name' => 'Test User',
                'email' => $email,
            ]);
        }

        $this->call([
            AgentSeeder::class,
        ]);
    }
}
