import os
path = "src/main/java/com/messaging/backend/auth/service/AuthService.java"
with open(path, "r") as f:
    content = f.read()

method = """
    public com.messaging.backend.auth.entity.User getUserById(java.util.UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new com.messaging.backend.common.exception.ResourceNotFoundException("User not found with id: " + userId));
    }
"""

# Insert before the last closing brace
last_brace_index = content.rfind('}')
new_content = content[:last_brace_index] + method + content[last_brace_index:]

with open(path, "w") as f:
    f.write(new_content)
