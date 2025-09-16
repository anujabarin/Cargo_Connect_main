<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class EventLog extends Model
{
    /** @use HasFactory<\Database\Factories\EventLogFactory> */
    use HasFactory;

    protected $fillable = [
        'agent_id',
        'event_type',
        'metadata',
    ];

    protected $casts = [
        'metadata' => 'array',
    ];
}
