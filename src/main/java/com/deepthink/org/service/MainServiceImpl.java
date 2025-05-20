package com.deepthink.org.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.deepthink.org.modals.Item;
import com.deepthink.org.repo.ItemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;

@Service
public class MainServiceImpl implements MainService {

	private final ChatClient.Builder clientBuilder;
	private final ItemRepository itemRepository;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public MainServiceImpl(ChatClient.Builder builder, ItemRepository itemRepository) {
		this.clientBuilder = builder;
		this.itemRepository = itemRepository;
	}

	@Override
	public Flux<String> getResByStream(String prompt, String mode, MultipartFile file) {
		return getResByStream(prompt, mode, file, "qwen2.5:14b"); // Default model
	}

	@Override
	public Flux<String> getResByStream(String prompt, String mode, MultipartFile file, String model) {
		System.out.println("Modal : " + model);
		OllamaOptions options = OllamaOptions.builder().model(model).build();
		ChatClient client = clientBuilder.defaultOptions(options).build();
		String aiPrompt = createAIPrompt(prompt, mode);

		if (file != null && !file.isEmpty()) {
			try {
				String fileText = extractFileContent(file);
				aiPrompt += "\n[Extracted Text]: " + fileText.substring(0, Math.min(fileText.length(), 1000));
			} catch (Exception e) {
				return Flux.error(new RuntimeException("Failed to process file", e));
			}
		}

		Prompt promptWithText = new Prompt(aiPrompt);
		return processAIResponse(client.prompt(promptWithText).stream().content());
	}

	@Override
	public Flux<String> getResByStream(String prompt, String mode) {
		return getResByStream(prompt, mode, null);
	}

	@Override
	public String getResSimple(String prompt, String mode) {
		return getResByStream(prompt, mode).blockFirst();
	}

	private String createAIPrompt(String prompt, String mode) {
		String basePrompt = switch (mode) {
		case "todo" ->
			String.format("You are an intelligent task management assistant. Analyze the user's query and determine:"
					+ "    • If it IS a CRUD operation, respond with exactly one JSON object in one of these formats:\n"
					+ "      • Create: {\"intent\": \"create\", \"content\": \"<task description>\"}\n"
					+ "      • Read:   {\"intent\": \"read\"}\n"
					+ "      • Update: {\"intent\": \"update\", \"id\": <id>, \"content\": \"<new task description>\"}\n"
					+ "      • Delete: {\"intent\": \"delete\", \"id\": <id>}\n\n"
					+ "    • If it is NOT a CRUD operation, answer conversationally.\n\nQuery: %s", prompt);
		default ->
			"You are an agentic AI assistant. Answer efficiently using markdown for formatting.\nQuery: " + prompt;
		};

		// Add specific formatting instructions based on query content
		String lowerPrompt = prompt.toLowerCase();
		if (lowerPrompt.contains("resume")) {
			basePrompt = "Provide a well-structured resume for: " + prompt
					+ "\nFormat the response using markdown with clear sections (e.g., headings, bullet points).";
		} else if (lowerPrompt.contains("algorithm") || lowerPrompt.contains("code")) {
			basePrompt = "Provide the response as a properly formatted code snippet for: " + prompt
					+ "\nUse markdown code fences with the appropriate language (e.g., ```java, ```python) and ensure the code block is fully closed.";
		} else if (lowerPrompt.contains("explain") || lowerPrompt.contains("describe")) {
			basePrompt = "Format the response in well-structured markdown paragraphs for: " + prompt
					+ "\nUse headings, lists, or other markdown elements as needed.";
		}

		return basePrompt;
	}

	private Flux<String> processAIResponse(Flux<String> aiResponse) {
		return Flux.create(sink -> {
			StringBuilder buffer = new StringBuilder();

			aiResponse.subscribe(chunk -> {
				String trimmed = chunk.trim();
				if (buffer.isEmpty() && !trimmed.startsWith("{")) {
					sink.next(chunk);
					return;
				}

				buffer.append(chunk);
				String accumulated = buffer.toString().trim();
				if (accumulated.startsWith("{") && accumulated.endsWith("}")) {
					try {
						JsonNode json = objectMapper.readTree(accumulated);
						handleCrudIntent(json, sink);
						buffer.setLength(0);
					} catch (IOException ignored) {
					}
				}
			}, sink::error, sink::complete);
		});
	}

	@Transactional
	private void handleCrudIntent(JsonNode json, reactor.core.publisher.FluxSink<String> sink) {
		System.out.println("Received JSON: " + json.toString());
		String intent = json.path("intent").asText();
		switch (intent) {
		case "create" -> {
			String content = json.path("content").asText();
			if (content == null || content.trim().isEmpty()) {
				JsonNode entity = json.path("entity");
				content = entity.path("item_name").asText();
				if (content == null || content.trim().isEmpty()) {
					sink.next("Error: Task content is missing or empty");
					return;
				}
			}
			Item item = new Item(content);
			Item savedItem = itemRepository.save(item);
			sink.next("Task created: " + savedItem.getContent() + " (ID: " + savedItem.getId() + ")");
			sink.next(getAllTasks());
		}
		case "read" -> sink.next(getAllTasks());
		case "update" -> {
			int id = json.path("id").asInt();
			String content = json.path("content").asText();
			if (content == null || content.trim().isEmpty()) {
				JsonNode entity = json.path("entity");
				content = entity.path("item_name").asText();
				if (content == null || content.trim().isEmpty()) {
					sink.next("Error: Task content is missing or empty");
					return;
				}
			}
			final String finalContent = content;
			itemRepository.findById(id).ifPresentOrElse(item -> {
				item.setContent(finalContent);
				Item updatedItem = itemRepository.save(item);
				sink.next("Task updated: " + updatedItem.getContent() + " (ID: " + id + ")\n");
			}, () -> sink.next("Task not found: ID " + id + "\n"));
			sink.next(getAllTasks());
		}
		case "delete" -> {
			int id = json.path("id").asInt();
			if (itemRepository.existsById(id)) {
				itemRepository.deleteById(id);
				sink.next("Task deleted: ID " + id + "\n");
			} else {
				sink.next("Task not found: ID " + id + "\n");
			}
			sink.next(getAllTasks());
		}
		default -> sink.next("Unknown intent: " + intent);
		}
	}

	private String getAllTasks() {
		var items = itemRepository.findAllByOrderByIdAsc();
		if (items.isEmpty()) {
			return "\nYou have no tasks.";
		}
		return "\nHere are your tasks:\n\n"
				+ items.stream().map(i -> i.getId() + ". " + i.getContent()).collect(Collectors.joining("\n\n"));
	}

	private String extractFileContent(MultipartFile file) throws IOException, InterruptedException {
		String ct = file.getContentType();
		if (ct != null && ct.startsWith("image/")) {
			return performOCR(file);
		} else if ("application/pdf".equals(ct)) {
			return extractTextFromPDF(file);
		} else {
			return new String(file.getBytes(), StandardCharsets.UTF_8);
		}
	}

	private String performOCR(MultipartFile file) throws IOException, InterruptedException {
		File tmp = File.createTempFile("ocr-", file.getOriginalFilename());
		file.transferTo(tmp);
		Process pb = new ProcessBuilder("C:\\Program Files\\Tesseract-OCR\\tesseract.exe", tmp.getAbsolutePath(),
				"stdout", "-l", "eng", "--psm", "6").redirectErrorStream(true).start();
		String result = new BufferedReader(new InputStreamReader(pb.getInputStream(), StandardCharsets.UTF_8)).lines()
				.collect(Collectors.joining("\n"));
		if (pb.waitFor() != 0) {
			throw new IOException("Tesseract failed");
		}
		tmp.delete();
		return result;
	}

	private String extractTextFromPDF(MultipartFile file) throws IOException {
		File tmp = File.createTempFile("pdf-", file.getOriginalFilename());
		file.transferTo(tmp);
		try (PDDocument doc = PDDocument.load(tmp)) {
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setStartPage(1);
			stripper.setEndPage(5);
			return stripper.getText(doc);
		} finally {
			tmp.delete();
		}
	}
}