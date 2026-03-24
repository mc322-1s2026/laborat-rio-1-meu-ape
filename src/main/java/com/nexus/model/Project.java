package com.nexus.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nexus.exception.NexusValidationException;

/**
 * Representa um projeto que agrupa tarefas e controla um orcamento em horas
 * Antes de adicionar uma tarefa, o projeto verifica se o orcamento nao vai estourar
 */
public class Project {
    private final String name;
    private final List<Task> tasks;
    private final double totalBudget; // orcamento maximo em horas

    /**
     * Cria um novo projeto com nome e orcamento
     *
     * @param name nome do projeto
     * @param totalBudget orcamento total em horas
     */
    public Project(String name, double totalBudget) {
        this.name = name;
        this.totalBudget = totalBudget;
        this.tasks = new ArrayList<>();
    }

    /**
     * Adiciona uma tarefa ao projeto, mas so se o orcamento permitir.
     * Soma o esforco de todas as tarefas existentes + a nova, e compara
     * com o budget total. Se ultrapassar, lanca excecao
     *
     * @param task tarefa a ser adicionada
     */
    public void addTask(Task task) {
        // Calcula quanto ja foi gasto somando o esforco de cada tarefa
        double horasUsadas = 0;
        for (Task t : tasks) {
            horasUsadas += t.getEstimatedEffort();
        }

        // Verifica se a nova tarefa cabe no orcamento
        if (horasUsadas + task.getEstimatedEffort() > totalBudget) {
            throw new NexusValidationException(
                "Orçamento excedido no projeto '" + name + "'. " +
                "Budget: " + totalBudget + "h, usado: " + horasUsadas +
                "h, tentando adicionar: " + task.getEstimatedEffort() + "h."
            );
        }

        tasks.add(task);
    }

    /**
     * Retorna a lista de tarefas do projeto
     *
     * @return lista nao modificavel de tarefas
     */
    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * Retorna o nome do projeto
     *
     * @return nome do projeto
     */
    public String getName() {
        return name;
    }

    /**
     * Retorna o orcamento total em horas
     *
     * @return budget em horas
     */
    public double getTotalBudget() {
        return totalBudget;
    }
}
