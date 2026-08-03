import React from 'react';

class ErrorBoundary extends React.Component {
  state = {
    hasError: false,
    error: null,
  };

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, info) {
    console.error('ErrorBoundary caught', error, info);
  }

  handleReset = () => {
    this.setState({ hasError: false, error: null });
  };

  render() {
    const { hasError, error } = this.state;
    const { children, Fallback } = this.props;

    if (hasError) {
      if (Fallback) {
        return <Fallback error={error} onReset={this.handleReset} />;
      }

      return (
        <div role="alert" className="error-fallback">
          <h2>Something went wrong</h2>
          <pre>{String(error?.message || error)}</pre>
          <button type="button" onClick={this.handleReset}>Try again</button>
        </div>
      );
    }

    return children;
  }
}

export function withErrorBoundary(Component, Fallback) {
  function WithErrorBoundary(props) {
    return (
      <ErrorBoundary Fallback={Fallback}>
        <Component {...props} />
      </ErrorBoundary>
    );
  }

  WithErrorBoundary.displayName = `withErrorBoundary(${Component.displayName || Component.name || 'Component'})`;
  return WithErrorBoundary;
}
