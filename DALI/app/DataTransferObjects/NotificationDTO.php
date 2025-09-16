<?php

namespace App\DataTransferObjects;

class NotificationDTO
{
    public function __construct(
        public int $agent_id,
        public string $agent_type,
        public string $notification_type,
        public string $message,
    ) {}

    public function toArray()
    {
        return [
            'agent_id' => $this->agent_id,
            'agent_type' => $this->agent_type,
            'notification_type' => $this->notification_type,
            'message' => $this->message,
        ];
    }
}
