# Development Workflow

Every feature, bug fix, and architectural change in this project must adhere to a strict development lifecycle to ensure high-quality software delivery and architectural compliance.

## The Complete Lifecycle

The development lifecycle follows these mandatory steps:

1. **Planning**: 
   - Define the feature or bug fix requirements.
   - Outline the necessary changes.
2. **Architecture Approval**: 
   - Before writing any code, the proposed changes must be reviewed and approved against `AGENTS.md`, the engineering constitution of this project.
3. **Implementation**: 
   - Develop the code in a dedicated branch following the branching strategy.
   - Ensure the implementation follows the modular monolith architecture, domain-oriented package structure, and all other principles outlined in `AGENTS.md`.
4. **Architecture Review**: 
   - A subsequent review phase to ensure the implementation strictly followed the approved architecture and did not introduce any violations (e.g., business logic in controllers).
5. **Testing**: 
   - All code must pass unit tests, integration tests, and end-to-end tests as appropriate for the layer being modified.
6. **Merge**: 
   - Once testing and reviews are successful, the code is merged into the appropriate target branch (`develop` or `main`).
7. **Release**: 
   - The code is deployed to the target environment following semantic versioning standards.
