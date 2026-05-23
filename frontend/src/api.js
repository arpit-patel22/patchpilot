// API client for PatchPilot backend
import axios from "axios";

// Create a configured axios instance
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  timeout: 60000, // 60s — Render free tier cold starts take ~30s
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
 * GET /api/kb
 * Returns knowledge base article summaries.
 * @param {Object} [opts]
 * @param {string} [opts.category] - Filter by category (e.g. "CORRUPTION")
 * @param {number} [opts.limit=8]  - Max results (1–50)
 */
export async function getKbArticles({ category, limit = 8 } = {}) {
  try {
    const params = {};
    if (category) params.category = category;
    if (limit) params.limit = limit;
    const { data } = await apiClient.get("/kb", { params });
    return data;
  } catch (error) {
    console.error("Failed to fetch KB articles:", error);
    return [];
  }
}

/**
 * GET /api/health
 * Simple liveness check.
 */
export async function getHealth() {
  const response = await apiClient.get("/health");
  return response.data;
}

/**
 * GET /api/kb/{id}
 * Returns full detail for a single knowledge base article.
 *
 * @param {number} id - Article ID
 * @returns {Promise<Object|null>} Article detail (id, title, software, osTarget,
 *   category, rootCause, resolutionSteps, createdAt), or null if not found.
 */
export async function getKbArticle(id) {
  try {
    const { data } = await apiClient.get(`/kb/${id}`);
    return data;
  } catch (error) {
    if (error.response?.status === 404) {
      console.warn(`KB article ${id} not found`);
      return null;
    }
    console.error(`Failed to fetch KB article ${id}:`, error);
    throw error;
  }
}