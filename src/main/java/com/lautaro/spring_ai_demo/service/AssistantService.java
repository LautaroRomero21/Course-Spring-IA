package com.lautaro.spring_ai_demo.service;

import com.lautaro.spring_ai_demo.repository.ConversationRepository;
import com.lautaro.spring_ai_demo.tools.HeladeraTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class AssistantService {

    private final ChatClient chatClient;
    private final HeladeraTools heladeraTools;
    private final ConversationRepository conversations;

    public AssistantService(ChatClient chatClient, HeladeraTools heladeraTools, ConversationRepository conversations) {
        this.chatClient = chatClient;
        this.heladeraTools = heladeraTools;
        this.conversations = conversations;
    }

    public String chat(String cid, String userText) {
        String safeCid = (cid == null || cid.isBlank()) ? "default" : cid;
        String safeUserText = (userText == null) ? "" : userText.trim();

        String history = conversations.get(safeCid).stream()
                .map(t -> t.role() + ": " + t.content())
                .collect(Collectors.joining("\n"));

        // Armamos el prompt base (RAG siempre habilitado, tools solo si corresponde)
        var prompt = chatClient.prompt()
                .system("""
                        Sos el asistente del programa de Heladeras Comunitarias.
                        - Para preguntas sobre politicas/reglas, apoyate en el contexto (RAG) si aparece.
                        - SOLO usa herramientas cuando el usuario te pida una ACCIoN explicita (listar/mostrar, reportar/crear, asignar).
                        - NUNCA crees incidentes ni hagas acciones si el usuario solo esta preguntando informacion.
                        - Si faltan datos para una accion (id de incidente, id de heladera, zona), pedi el dato puntual.
                        - Responde SIEMPRE en español, breve y claro.

                        Historial reciente:
                        {history}
                        """
                        .replace("{history}", history))
                .advisors(a -> a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, "type == 'kb'"))
                .user(safeUserText);

        // Tool-gating: habilitamos tools solo cuando el texto parece una accion
        if (isActionIntent(safeUserText)) {
            prompt = prompt.tools(heladeraTools);
        }

        String answer = prompt.call().content();

        // Persistimos el turno
        conversations.append(safeCid, "user", safeUserText);
        conversations.append(safeCid, "assistant", answer);

        return answer;
    }

    /**
     * Heuristica simple para decidir si habilitamos Tools.
     * La idea es evitar "acciones fantasma" cuando el usuario solo pide informacion
     * (RAG).
     */
    private boolean isActionIntent(String text) {
        if (text == null)
            return false;
        String q = text.toLowerCase();

        // verbos de accion tipicos
        boolean asksToReport = q.contains("report") || q.contains("reporta") || q.contains("reporta")
                || q.contains("crear incidente") || q.contains("crea incidente") || q.contains("crea incidente")
                || q.contains("abrir incidente") || q.contains("abri incidente") || q.contains("abre incidente");

        boolean asksToAssign = q.contains("asign") || q.contains("asigna") || q.contains("asigna")
                || q.contains("deriv") || q.contains("deriva") || q.contains("deriva");

        boolean asksToList = q.contains("list") || q.contains("listame") || q.contains("lista")
                || q.contains("mostr") || q.contains("mostrame") || q.contains("mostra")
                || q.contains("ver ") || q.contains("dame ");

        // Si menciona "incidente" pero NO hay verbo de accion, lo tomamos como consulta
        // informativa
        boolean mentionsIncidente = q.contains("incidente");

        // Accion si hay verbos claros; "incidente" solo no alcanza.
        return asksToReport || asksToAssign || asksToList || (mentionsIncidente && (asksToReport || asksToAssign));
    }
}
