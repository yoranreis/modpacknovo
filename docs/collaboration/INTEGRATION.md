# Regras de Integração e Resolução de Conflitos (INTEGRATION.md)

Este documento define o fluxo de trabalho obrigatório para contribuições no repositório.

---

## 1. Regras Fundamentais de Git

1. **A branch `main` é sagrada:** Apenas código estável, testado e aprovado pelo Desenvolvedor Principal reside na `main`.
2. **Push direto na `main` é proibido para o auxiliar:** Toda contribuição do colaborador auxiliar deve partir de uma branch `feat/<nome-da-tarefa>`.
3. **Sem comandos destrutivos:** Nunca execute `git reset --hard`, `git push --force` ou `git clean -fd`.

---

## 2. Fluxo de Trabalho do Colaborador Auxiliar

1. **Atualizar a base:**
   ```bash
   git checkout main
   git pull origin main
   ```
2. **Criar branch de tarefa:**
   ```bash
   git checkout -b feat/nome-da-tarefa
   ```
3. **Desenvolver dentro dos arquivos autorizados** (ver `OWNERSHIP.md`).
4. **Validar localmente antes de enviar:**
   ```bash
   .\gradlew.bat test
   .\gradlew.bat build
   ```
5. **Enviar branch e solicitar integração:**
   ```bash
   git push origin feat/nome-da-tarefa
   ```

---

## 3. Fluxo de Auditoria e Merge pelo Desenvolvedor Principal

1. O Principal inspeciona as alterações remotas:
   ```bash
   git fetch origin
   git log -p main..origin/feat/nome-da-tarefa
   ```
2. O Principal valida se algum arquivo restrito foi modificado sem autorização.
3. O Principal testa a branch localmente em ambiente isolado:
   ```bash
   git checkout -b review/feat-nome origin/feat/nome-da-tarefa
   .\gradlew.bat test
   .\gradlew.bat build
   ```
4. Se aprovado, o Principal faz o merge na `main` e sobe:
   ```bash
   git checkout main
   git merge review/feat-nome --no-ff -m "merge(feat): integrar <nome-da-tarefa>"
   git push origin main
   ```
5. O auxiliar rebaseia sua branch na `main` atualizada.
