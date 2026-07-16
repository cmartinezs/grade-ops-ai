import { z } from "zod";

export const briefSchema = z.object({
  learningGoal: z.string().min(1, "Ingresa el objetivo de aprendizaje."),
  topic: z.string().min(1, "Ingresa el tema."),
  level: z.string().min(1, "Ingresa el nivel."),
  duration: z.string().min(1, "Ingresa la duración."),
  language: z.string().min(1, "Ingresa el idioma."),
});

export type BriefFormValues = z.infer<typeof briefSchema>;
