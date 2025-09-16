<?php

namespace App\Http\Controllers;

use App\Http\Requests\StoreEventLogRequest;
use App\Http\Requests\UpdateEventLogRequest;
use App\Models\EventLog;

class EventLogController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        //
    }

    /**
     * Show the form for creating a new resource.
     */
    public function create()
    {
        //
    }

    /**
     * Store a newly created resource in storage.
     */
    public function store(StoreEventLogRequest $request)
    {
        //
    }

    /**
     * Display the specified resource.
     */
    public function show(EventLog $eventLog)
    {
        //
    }

    /**
     * Show the form for editing the specified resource.
     */
    public function edit(EventLog $eventLog)
    {
        //
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(UpdateEventLogRequest $request, EventLog $eventLog)
    {
        //
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(EventLog $eventLog)
    {
        //
    }
}
