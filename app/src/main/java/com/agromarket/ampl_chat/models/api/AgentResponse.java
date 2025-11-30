package com.agromarket.ampl_chat.models.api;

public class AgentResponse {
    public boolean status;
    public int agent_id;   // null if not assigned
    public String message;     // optional, may contain "Agent not assigned yet"
}