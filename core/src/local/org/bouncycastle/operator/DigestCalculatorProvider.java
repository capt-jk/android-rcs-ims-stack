package local.org.bouncycastle.operator;

import local.org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public interface DigestCalculatorProvider
{
    DigestCalculator get(AlgorithmIdentifier digestAlgorithmIdentifier)
        throws OperatorCreationException;
}
