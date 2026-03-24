package com.nexus.model;

import java.time.LocalDate;

import com.nexus.exception.NexusValidationException;

/**
 * Representa uma tarefa no sistema
 * Funciona como uma maquina de estados: TO_DO -> IN_PROGRESS -> DONE
 * Uma tarefa pode ser BLOCKED a partir de qualquer estado, exceto DONE
 */
public class Task {
    // Contadores globais compartilhados por todas as tarefas
    public static int totalTasksCreated = 0;
    public static int totalValidationErrors = 0;
    public static int activeWorkload = 0;

    private static int nextId = 1;

    // final = nao pode ser alterado depois de criado
    private final int id;
    private final LocalDate deadline;

    private String title;
    private TaskStatus status;
    private User owner;
    private double estimatedEffort; // esforco estimado em horas

    /**
     * Cria uma nova tarefa com titulo e prazo
     * O status inicial e sempre TO_DO e o id e gerado automaticamente
     *
     * @param title titulo da tarefa
     * @param deadline prazo de entrega
     */
    public Task(String title, LocalDate deadline) {
        this.id = nextId++;
        this.deadline = deadline;
        this.title = title;
        this.status = TaskStatus.TO_DO;
        this.estimatedEffort = 0;

        totalTasksCreated++;
    }

    /**
     * Move a tarefa para IN_PROGRESS.
     * So e permitido se houver um usuario atribuido e a tarefa nao estiver BLOCKED
     *
     * @param user usuario que vai assumir a tarefa
     */
    public void moveToInProgress(User user) {
        // Precisa ter um dono para poder comecar a trabalhar
        if (user == null) {
            totalValidationErrors++;
            throw new NexusValidationException("Tarefa precisa de um owner para ir para IN_PROGRESS.");
        }

        // Tarefa bloqueada nao pode ser movida
        if (this.status == TaskStatus.BLOCKED) {
            totalValidationErrors++;
            throw new NexusValidationException("Tarefa BLOCKED não pode ir para IN_PROGRESS.");
        }

        this.owner = user;
        this.status = TaskStatus.IN_PROGRESS;
        activeWorkload++;
    }

    /**
     * Finaliza a tarefa movendo para DONE.
     * Nao e permitido se a tarefa estiver BLOCKED
     */
    public void markAsDone() {
        if (this.status == TaskStatus.BLOCKED) {
            totalValidationErrors++;
            throw new NexusValidationException("Tarefa BLOCKED não pode ser finalizada.");
        }

        // Se estava em progresso, diminui o contador de trabalho ativo
        if (this.status == TaskStatus.IN_PROGRESS) {
            activeWorkload--;
        }

        this.status = TaskStatus.DONE;
    }

    /**
     * Bloqueia ou desbloqueia a tarefa.
     * Uma tarefa DONE nao pode ser bloqueada (ja foi concluida)
     *
     * @param blocked true para bloquear, false para desbloquear
     */
    public void setBlocked(boolean blocked) {
        if (blocked) {
            // Tarefa concluida nao volta atras
            if (this.status == TaskStatus.DONE) {
                totalValidationErrors++;
                throw new NexusValidationException("Tarefa DONE não pode ser bloqueada.");
            }

            // Se estava em progresso, diminui o workload antes de bloquear
            if (this.status == TaskStatus.IN_PROGRESS) {
                activeWorkload--;
            }

            this.status = TaskStatus.BLOCKED;
        } else {
            this.status = TaskStatus.TO_DO;
        }
    }

    /**
     * Atribui um dono a tarefa
     *
     * @param owner usuario que sera o responsavel
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    /**
     * Define o esforco estimado em horas
     *
     * @param effort horas estimadas para completar a tarefa
     */
    public void setEstimatedEffort(double effort) {
        this.estimatedEffort = effort;
    }

    // Getters
    public int getId() { return id; }
    public TaskStatus getStatus() { return status; }
    public String getTitle() { return title; }
    public LocalDate getDeadline() { return deadline; }
    public User getOwner() { return owner; }
    public double getEstimatedEffort() { return estimatedEffort; }
}
