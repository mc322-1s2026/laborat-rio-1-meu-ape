package com.nexus.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.nexus.exception.NexusValidationException;
import com.nexus.model.Project;
import com.nexus.model.Task;
import com.nexus.model.User;

/**
 * Processa arquivos de log com comandos para o sistema Nexus.
 * Le cada linha do arquivo, interpreta o comando e executa a acao correspondente
 * Se um comando falhar, captura o erro e continua processando as proximas linhas
 */
public class LogProcessor {

    // Lista de projetos criados durante o processamento do log
    private final List<Project> projects = new ArrayList<>();
    /**
     * Retorna os projetos criados durante o processamento.
     *
     * @return lista de projetos
     */
    public List<Project> getProjects() {
        return projects;
    }

    /**
     * Le um arquivo de log e executa cada comando encontrado.
     * Formato das linhas: COMANDO;PARAM1;PARAM2;...
     *
     * @param fileName nome do arquivo dentro de resources
     * @param workspace workspace onde as tarefas sao armazenadas
     * @param users lista de usuarios do sistema
     */
    public void processLog(String fileName, Workspace workspace, List<User> users) {
        try {
            var resource = getClass().getClassLoader().getResourceAsStream(fileName);

            if (resource == null) {
                throw new IOException("Arquivo não encontrado no classpath: " + fileName);
            }

            try (java.util.Scanner s = new java.util.Scanner(resource).useDelimiter("\\A")) {
                String content = s.hasNext() ? s.next() : "";
                List<String> lines = List.of(content.split("\\R"));

                for (String line : lines) {
                    // Ignora linhas vazias e comentarios
                    if (line.isBlank() || line.startsWith("#")) continue;

                    String[] p = line.split(";");
                    String action = p[0];

                    try {
                        switch (action) {
                            case "CREATE_USER" -> {
                                users.add(new User(p[1], p[2]));
                                System.out.println("[LOG] Usuário criado: " + p[1]);
                            }

                            case "CREATE_PROJECT" -> {
                                double budget = Double.parseDouble(p[2]);
                                Project proj = new Project(p[1], budget);
                                projects.add(proj);
                                System.out.println("[LOG] Projeto criado: " + p[1]);
                            }

                            case "CREATE_TASK" -> {
                                // Formato pode ser simples (nome;data) ou completo (nome;data;esforco;projeto)
                                Task t = new Task(p[1], LocalDate.parse(p[2]));
                                workspace.addTask(t);

                                // Se tem esforco e projeto, vincula ao projeto
                                if (p.length >= 5) {
                                    t.setEstimatedEffort(Double.parseDouble(p[3]));
                                    String projectName = p[4];

                                    // Procura o projeto pelo nome na lista
                                    Project found = findProjectByName(projectName);
                                    if (found != null) {
                                        found.addTask(t);
                                    } else {
                                        System.err.println("[WARN] Projeto não encontrado: " + projectName);
                                    }
                                }

                                System.out.println("[LOG] Tarefa criada: " + p[1]);
                            }

                            case "ASSIGN_USER" -> {
                                int taskId = Integer.parseInt(p[1]);
                                String username = p[2];

                                // Procura a tarefa pelo ID e o usuario pelo username
                                Task task = findTaskById(taskId, workspace);
                                User user = findUserByUsername(username, users);

                                if (task == null) {
                                    throw new NexusValidationException("Tarefa com ID " + taskId + " não encontrada.");
                                }
                                if (user == null) {
                                    throw new NexusValidationException("Usuário '" + username + "' não encontrado.");
                                }

                                task.setOwner(user);
                                System.out.println("[LOG] Usuário " + username + " atribuído à tarefa " + taskId);
                            }

                            case "CHANGE_STATUS" -> {
                                int taskId = Integer.parseInt(p[1]);
                                String newStatus = p[2];

                                Task task = findTaskById(taskId, workspace);
                                if (task == null) {
                                    throw new NexusValidationException("Tarefa com ID " + taskId + " não encontrada.");
                                }

                                // Chama o metodo correto de acordo com o status desejado
                                switch (newStatus) {
                                    case "IN_PROGRESS" -> task.moveToInProgress(task.getOwner());
                                    case "DONE" -> task.markAsDone();
                                    case "BLOCKED" -> task.setBlocked(true);
                                    case "TO_DO" -> task.setBlocked(false); // desbloqueia voltando pra TO_DO
                                    default -> System.err.println("[WARN] Status desconhecido: " + newStatus);
                                }

                                System.out.println("[LOG] Tarefa " + taskId + " movida para " + newStatus);
                            }

                            case "REPORT_STATUS" -> {
                                workspace.printReports(users, projects);
                            }

                            default -> System.err.println("[WARN] Ação desconhecida: " + action);
                        }
                    } catch (NexusValidationException e) {
                        System.err.println("[ERRO DE REGRAS] Falha no comando '" + line + "': " + e.getMessage());
                    } catch (Exception e) {
                        // Captura qualquer outro erro (formato invalido, numero errado, etc)
                        // para nao travar o processamento do restante do log
                        System.err.println("[ERRO] Falha ao processar '" + line + "': " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[ERRO FATAL] " + e.getMessage());
        }
    }

    /**
     * Busca uma tarefa pelo ID dentro do workspace
     *
     * @param id id da tarefa procurada
     * @param workspace workspace com as tarefas
     * @return a tarefa encontrada, ou null se nao existir
     */
    private Task findTaskById(int id, Workspace workspace) {
        for (Task t : workspace.getTasks()) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    /**
     * Busca um usuario pelo username na lista
     *
     * @param username username procurado
     * @param users lista de usuarios
     * @return o usuario encontrado, ou null se nao existir
     */
    private User findUserByUsername(String username, List<User> users) {
        for (User u : users) {
            if (u.consultUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Busca um projeto pelo nome na lista interna
     *
     * @param name nome do projeto procurado
     * @return o projeto encontrado, ou null se nao existir
     */
    private Project findProjectByName(String name) {
        for (Project p : projects) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }
}
