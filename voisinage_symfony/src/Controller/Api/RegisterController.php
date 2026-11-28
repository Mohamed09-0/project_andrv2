<?php

namespace App\Controller\Api;

use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;

class RegisterController extends AbstractController
{
    #[Route('/api/register', name: 'app_api_register', methods: ['POST'])]
    public function register(
        Request $request,
        EntityManagerInterface $entityManager
    ): JsonResponse {

        $data = json_decode($request->getContent(), true);

        if (!is_array($data)) {
            return $this->json(['success' => false, 'message' => 'Invalid JSON'], 400);
        }

        $nom = trim($data['nom'] ?? '');
        $prenom = trim($data['prenom'] ?? '');
        $email = trim($data['email'] ?? '');
        $telephone = trim($data['telephone'] ?? '');
        $login = trim($data['login'] ?? '');
        $password = $data['password'] ?? '';
        $confirmPassword = $data['confirmPassword'] ?? '';

        if (!$nom || !$prenom || !$email || !$telephone || !$login || !$password || !$confirmPassword) {
            return $this->json(['success' => false, 'message' => 'All fields are required'], 400);
        }

        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            return $this->json(['success' => false, 'message' => 'Invalid email'], 400);
        }

        if (strlen($password) < 8) {
            return $this->json(['success' => false, 'message' => 'Password must contain at least 8 characters'], 400);
        }

        if ($password !== $confirmPassword) {
            return $this->json(['success' => false, 'message' => 'Passwords do not match'], 400);
        }

        $existingUser = $entityManager->getRepository(User::class)->findOneBy(['email' => $email]);

        if ($existingUser) {
            return $this->json(['success' => false, 'message' => 'Email already exists'], 409);
        }

        $user = new User();
        $user->setNom($nom);
        $user->setPrenom($prenom);
        $user->setEmail($email);
        $user->setTelephone($telephone);
        $user->setLogin($login);
        $user->setMotDePasseHash(password_hash($password, PASSWORD_DEFAULT));
        $user->setRole('utilisateur');

        $entityManager->persist($user);
        $entityManager->flush();

        return $this->json([
            'success' => true,
            'message' => 'Account created successfully'
        ], 201);
    }
}