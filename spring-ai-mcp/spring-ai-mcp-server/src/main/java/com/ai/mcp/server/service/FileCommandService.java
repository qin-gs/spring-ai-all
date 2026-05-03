package com.ai.mcp.server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class FileCommandService {

    @Tool(name = "readFile", description = "读取指定路径的文件内容")
    public String readFile(@ToolParam(description = "文件路径") String filePath) throws IOException {
        Path path = Paths.get(filePath).normalize();
        log.info("读取文件: {}", path);
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    @Tool(name = "writeFile", description = "将内容写入指定路径的文件")
    public String writeFile(
            @ToolParam(description = "文件路径") String filePath,
            @ToolParam(description = "写入内容") String content) throws IOException {
        Path path = Paths.get(filePath).normalize();
        log.info("写入文件: {}", path);
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
        return "文件写入成功: " + path;
    }

    private static final List<String> DENIED_COMMANDS = List.of(
            "rm", "dd", "mkfs", "format", "shutdown", "reboot", "poweroff",
            "halt", "init", "fdisk", "parted", "mkswap", "swapoff",
            "chown", "chmod", "chattr", "mkfs.ext4", "mkfs.xfs",
            ":(){", "fork", "> /dev/sd", "> /dev/nvme", "> /dev/mmcblk",
            "mv /", "cp /", "ln -sf /",
            "wget", "curl", "nc ", "netcat",
            "iptables", "ufw", "route",
            "passwd", "useradd", "userdel", "usermod",
            "sudo", "su -", "pkexec"
    );

    @Tool(name = "executeCommand", description = "在本地执行 Linux 命令并返回结果")
    public String executeCommand(@ToolParam(description = "要执行的命令") String command) {
        log.info("执行命令: {}", command);

        String trimmed = command.strip();
        for (String denied : DENIED_COMMANDS) {
            if (trimmed.contains(denied)) {
                log.warn("拦截危险命令: {} (匹配到: {})", trimmed, denied);
                return "命令被拦截: 包含危险操作 (" + denied + ")，已禁止执行";
            }
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                return "命令执行超时（30秒）";
            }

            int exitCode = process.exitValue();
            return "退出码: " + exitCode + "\n输出:\n" + output;
        } catch (Exception e) {
            log.error("命令执行失败", e);
            return "命令执行失败: " + e.getMessage();
        }
    }

    @Tool(name = "grep", description = "在文件中搜索匹配指定模式的行")
    public String grep(
            @ToolParam(description = "文件路径") String filePath,
            @ToolParam(description = "搜索模式（正则表达式）") String pattern) {
        log.info("在文件 {} 中搜索模式: {}", filePath, pattern);
        String result = executeCommand("grep -n -- '" + pattern + "' '" + filePath + "'");
        if (result.contains("退出码: 0")) {
            return "匹配结果:\n" + result.substring(result.indexOf("\n") + 1);
        } else if (result.contains("退出码: 1")) {
            return "未找到匹配行";
        } else {
            return result;
        }
    }
}
