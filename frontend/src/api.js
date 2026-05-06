// API client for PatchPilot backend
import axios from "axios";

// Base URL — points to local Spring Boot backend during dev
// In production, this would be set via environment variable
const API_BASE = "http://localhost:8080/api";

// Create a configured axios instance
const apiClient = axios.create({
  baseURL: API_BASE,
  timeout: 30000, // 30s — Claude API calls can be slow
  headers: {
    "Content-Type": "application/json",
  },
});

/**
 * POST /api/diagnose
 * Submits a troubleshooting request to the Claude-powered backend.
 *
 * @param {Object} request - Diagnostic input
 * @param {string} request.software - e.g. "Visual Studio Code"
 * @param {string} request.softwareVersion - e.g. "1.88"
 * @param {string} request.osVersion - e.g. "Windows 11"
 * @param {string} request.errorMessage - Full error log
 * @param {string} request.userAttempts - What user already tried
 * @param {string} request.severity - LOW | MEDIUM | HIGH | BLOCKING
 * @returns {Promise<Object>} Diagnosis response with rankedCauses and stepsToFix
 */
export async function diagnose(request) {
  const response = await apiClient.post("/diagnose", request);
  return response.data;
}

/**
 * GET /api/tickets
 * Returns all troubleshooting tickets ordered by createdAt desc.
 */
export async function getTickets() {
  const response = await apiClient.get("/tickets");
  return response.data;
}

/**
 * GET /api/health
 * Simple liveness check.
 */
export async function getHealth() {
  const response = await apiClient.get("/health");
  return response.data;
}