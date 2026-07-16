interface FormProps extends React.FormHTMLAttributes<HTMLFormElement> {
  children: React.ReactNode;
}

export default function Form({ children, style, ...props }: FormProps) {
  return (
    <form noValidate style={{ display: "flex", flexDirection: "column", gap: 16, ...style }} {...props}>
      {children}
    </form>
  );
}
