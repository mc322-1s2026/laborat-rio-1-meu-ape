package com.nexus.model;

import java.util.List;

/**
 * Representa um usuario no sistema Nexus.
 * Cada usuario tem um username unico e um email validado
 */
public class User {
    private final String username;
    private final String email;

    /**
     * Cria um novo usuario com validacao de dados.
     * O username nao pode ser vazio e o email precisa conter '@'
     *
     * @param username nome do usuario
     * @param email email do usuario
     */
    public User(String username, String email) {
        // Valida username: nao pode ser nulo nem vazio
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username não pode ser vazio.");
        }

        // Valida email: precisa existir e ter o formato basico com '@'
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email inválido.");
        }

        this.username = username;
        this.email = email;
    }

    /**
     * Retorna o email do usuario.
     *
     * @return email cadastrado
     */
    public String consultEmail() {
        return email;
    }

    /**
     * Retorna o username do usuario.
     *
     * @return username cadastrado
     */
    public String consultUsername() {
        return username;
    }

    /**
     * Calcula quantas tarefas IN_PROGRESS esse usuario possui.
     * Recebe a lista de todas as tarefas do sistema e filtra
     * apenas as que estao em progresso e pertencem a este usuario.
     *
     * @param allTasks lista com todas as tarefas do workspace
     * @return quantidade de tarefas em andamento deste usuario
     */
    public long calculateWorkload(List<Task> allTasks) {
        return allTasks.stream()
            .filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS)
            .filter(t -> t.getOwner() != null && t.getOwner().equals(this))
            .count();
    }

    /**
     * Compara dois usuarios pelo username, que e o identificador unico
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.username);
    }

    /**
     * Gera o hashCode baseado no username para ser consistente com equals
     */
    @Override
    public int hashCode() {
        return username.hashCode();
    }
}
