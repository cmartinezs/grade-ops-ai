"use client";

import { useEffect } from "react";
import { useForm, type FieldValues, type Path, type Resolver } from "react-hook-form";
import Form from "./Form";
import Field from "./Field";
import Input from "./Input";
import Textarea from "./Textarea";
import Select from "./Select";
import Checkbox from "./Checkbox";

export interface FieldDefinition {
  name: string;
  label: string;
  control: "input" | "textarea" | "select" | "checkbox";
  required?: boolean;
  placeholder?: string;
  options?: { value: string; label: string }[];
}

interface DynamicFormProps<T extends FieldValues> {
  fields: FieldDefinition[];
  onSubmit: (values: T) => void;
  resolver?: Resolver<T>;
  externalErrors?: Partial<Record<keyof T, string>>;
}

export default function DynamicForm<T extends FieldValues>({
  fields,
  onSubmit,
  resolver,
  externalErrors,
}: DynamicFormProps<T>) {
  const {
    register,
    handleSubmit,
    setError,
    clearErrors,
    formState: { errors },
  } = useForm<T>({ resolver });

  useEffect(() => {
    if (!externalErrors) return;
    for (const [name, message] of Object.entries(externalErrors)) {
      if (message) {
        setError(name as Path<T>, { type: "server", message: message as string });
      }
    }
  }, [externalErrors, setError]);

  // RHF only auto-revalidates fields that already failed through the resolver;
  // a manually `setError`'d server error needs an explicit clear on the next edit.
  function registerField(name: Path<T>) {
    const registration = register(name);
    return {
      ...registration,
      onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
        const result = registration.onChange(event);
        if (errors[name]?.type === "server") {
          clearErrors(name);
        }
        return result;
      },
    };
  }

  return (
    <Form onSubmit={handleSubmit(onSubmit)}>
      {fields.map((field) => {
        const fieldError = errors[field.name]?.message as string | undefined;
        const isCheckbox = field.control === "checkbox";

        return (
          <Field
            key={field.name}
            label={isCheckbox ? undefined : field.label}
            htmlFor={field.name}
            required={isCheckbox ? false : field.required}
            error={fieldError}
          >
            {field.control === "input" && (
              <Input
                id={field.name}
                placeholder={field.placeholder}
                error={fieldError}
                {...registerField(field.name as Path<T>)}
              />
            )}
            {field.control === "textarea" && (
              <Textarea
                id={field.name}
                placeholder={field.placeholder}
                error={fieldError}
                {...registerField(field.name as Path<T>)}
              />
            )}
            {field.control === "select" && (
              <Select id={field.name} error={fieldError} {...registerField(field.name as Path<T>)}>
                {field.options?.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </Select>
            )}
            {isCheckbox && (
              <Checkbox
                id={field.name}
                label={field.label}
                required={field.required}
                {...registerField(field.name as Path<T>)}
              />
            )}
          </Field>
        );
      })}
    </Form>
  );
}
