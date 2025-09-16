<?php

use App\Http\Controllers\Api\AgentController;
use Illuminate\Support\Facades\Artisan;
// use App\Livewire\Dashboard;
use Illuminate\Support\Facades\Route;
// use App\Http\Controllers\DashboardController;
use Livewire\Volt\Volt;

Route::get('/', function () {
    // return view('welcome');
    return redirect()->route('dashboard');
})->name('home');

Route::view('dashboard', 'dashboard')
    ->middleware(['auth', 'verified'])
    ->name('dashboard');
// Route::get('/dashboard', fn () => view('dashboard'));

Route::get('/live-agents', [AgentController::class, 'getAgentStates']);

Route::middleware(['auth'])->group(function () {
    Route::redirect('settings', 'settings/profile');

    Volt::route('settings/profile', 'settings.profile')->name('settings.profile');
    Volt::route('settings/password', 'settings.password')->name('settings.password');
    Volt::route('settings/appearance', 'settings.appearance')->name('settings.appearance');
});

Route::post('/simulate/scenario/{name}', function ($name) {
    Artisan::call("app:simulate-scenario {$name}");

    return response()->json(['status' => 'ok']);
});

require __DIR__.'/auth.php';

// Route::get('/', Dashboard::class);
Route::get('/map', fn () => view('map'));

Route::post('/driver/location', [AgentController::class, 'notifyNextAgents']);

// API
Route::group([
    'prefix' => 'api',
    'as' => 'api.',
], function () {
    Route::get('agents', [\App\Http\Controllers\Api\AgentController::class, 'index'])->name('agents.index');
    // Route::get('agents/{agent}', [\App\Http\Controllers\Api\AgentController::class, 'show'])->name('agents.show');
    // Route::post('agents', [\App\Http\Controllers\Api\AgentController::class, 'store'])->name('agents.store');
    // Route::put('agents/{agent}', [\App\Http\Controllers\Api\AgentController::class, 'update'])->name('agents.update');
    // Route::delete('agents/{agent}', [\App\Http\Controllers\Api\AgentController::class, 'destroy'])->name('agents.destroy');

    Route::get('/agents', [AgentController::class, 'index']);
    Route::get('/agents/{agent}/notifications', [AgentController::class, 'notifications']);
});
