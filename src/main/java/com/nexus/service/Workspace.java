package com.nexus.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.nexus.model.Project;
import com.nexus.model.Task;
import com.nexus.model.TaskStatus;
import com.nexus.model.User;

/**
 * Container principal do sistema que armazena todas as tarefas
 * e oferece metodos de busca, filtragem e relatorios
 */
public class Workspace {
    private final List<Task> tasks = new ArrayList<>();

    /**
     * Adiciona uma tarefa ao workspace
     *
     * @param task tarefa a ser adicionada
     */
    public void addTask(Task task) {
        tasks.add(task);
    }

    /**
     * Retorna a lista de tarefas protegida contra modificacoes externas
     *
     * @return lista nao modificavel de tarefas
     */
    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * Retorna os 3 usuarios com mais tarefas concluidas (DONE)
     * Usa Stream API para filtrar, agrupar e ordenar
     *
     * @param users lista de todos os usuarios do sistema
     * @return lista com ate 3 top performers
     */
    public List<User> getTopPerformers(List<User> users) {
        // Para cada usuario, conta quantas tarefas DONE ele tem,
        // ordena do maior pro menor e pega os 3 primeiros
        return users.stream()
            .sorted((u1, u2) -> {
                long done1 = tasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.DONE)
                    .filter(t -> t.getOwner() != null && t.getOwner().equals(u1))
                    .count();
                long done2 = tasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.DONE)
                    .filter(t -> t.getOwner() != null && t.getOwner().equals(u2))
                    .count();
                return Long.compare(done2, done1); // decrescente
            })
            .limit(3)
            .collect(Collectors.toList());
    }

    /**
     * Lista usuarios sobrecarregados (mais de 10 tarefas IN_PROGRESS).
     *
     * @param users lista de todos os usuarios do sistema
     * @return lista de usuarios sobrecarregados
     */
    public List<User> getOverloadedUsers(List<User> users) {
        return users.stream()
            .filter(u -> u.calculateWorkload(tasks) > 10)
            .collect(Collectors.toList());
    }

    /**
     * Calcula o percentual de conclusao de um projeto
     * Divide a quantidade de tarefas DONE pelo total de tarefas
     *
     * @param project projeto a ser analisado
     * @return percentual de 0.0 a 100.0
     */
    public double getProjectHealth(Project project) {
        List<Task> projectTasks = project.getTasks();

        if (projectTasks.isEmpty()) {
            return 0.0;
        }

        long concluidas = projectTasks.stream()
            .filter(t -> t.getStatus() == TaskStatus.DONE)
            .count();

        return (concluidas * 100.0) / projectTasks.size();
    }

    /**
     * Identifica qual status (exceto DONE) tem mais tarefas no sistema
     * Util para encontrar gargalos no fluxo de trabalho
     *
     * @return o status que mais acumula tarefas, ou null se nao houver tarefas
     */
    public TaskStatus getGlobalBottleneck() {
        // Agrupa tarefas por status, ignorando DONE
        Map<TaskStatus, Long> contagem = tasks.stream()
            .filter(t -> t.getStatus() != TaskStatus.DONE)
            .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));

        // Encontra o status com maior quantidade
        return contagem.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    /**
     * Imprime todos os relatorios no console
     *
     * @param users lista de usuarios do sistema
     * @param projects lista de projetos do sistema
     */
    public void printReports(List<User> users, List<Project> projects) {
        System.out.println("\n===== RELATORIO NEXUS =====");

        // Top Performers
        System.out.println("\n-- Top 3 Performers (mais tarefas DONE) --");
        List<User> top = getTopPerformers(users);
        for (User u : top) {
            long done = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .filter(t -> t.getOwner() != null && t.getOwner().equals(u))
                .count();
            System.out.println("  " + u.consultUsername() + ": " + done + " tarefas concluídas");
        }

        // Overloaded Users
        System.out.println("\n-- Usuarios Sobrecarregados (>10 tarefas IN_PROGRESS) --");
        List<User> overloaded = getOverloadedUsers(users);
        if (overloaded.isEmpty()) {
            System.out.println("  Nenhum usuario sobrecarregado.");
        } else {
            for (User u : overloaded) {
                System.out.println("  " + u.consultUsername() + ": " + u.calculateWorkload(tasks) + " tarefas em andamento");
            }
        }

        // Project Health
        System.out.println("\n-- Saude dos Projetos --");
        for (Project p : projects) {
            double health = getProjectHealth(p);
            System.out.printf("  %s: %.1f%% concluído%n", p.getName(), health);
        }

        // Global Bottleneck
        System.out.println("\n-- Gargalo Global --");
        TaskStatus bottleneck = getGlobalBottleneck();
        if (bottleneck != null) {
            System.out.println("  Status com mais tarefas acumuladas: " + bottleneck);
        } else {
            System.out.println("  Nenhuma tarefa no sistema.");
        }

        System.out.println("\n===========================");
    }
}
