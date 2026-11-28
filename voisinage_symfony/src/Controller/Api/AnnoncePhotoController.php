<?php

namespace App\Controller\Api;

use App\Entity\Annonce;
use App\Entity\PhotoAnnonce;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;

class AnnoncePhotoController extends AbstractController
{
    #[Route(
        '/api/annonces/{id}/photos',
        name: 'api_annonce_add_photo',
        methods: ['POST']
    )]
    public function addPhoto(
        int $id,
        Request $request,
        EntityManagerInterface $entityManager
    ): JsonResponse {

        // 1. Find announcement
        $annonce = $entityManager
            ->getRepository(Annonce::class)
            ->find($id);

        if (!$annonce) {
            return $this->json([
                'success' => false,
                'message' => 'Announcement not found'
            ], 404);
        }

        // 2. Get uploaded photo
        $photoFile = $request->files->get('photo');

        if (!$photoFile) {
            return $this->json([
                'success' => false,
                'message' => 'No photo was received'
            ], 400);
        }

        // 3. Check upload validity
        if (!$photoFile->isValid()) {
            return $this->json([
                'success' => false,
                'message' => 'Photo upload is invalid'
            ], 400);
        }

        // 4. Check file size (5 MB maximum)
        if ($photoFile->getSize() > 5 * 1024 * 1024) {
            return $this->json([
                'success' => false,
                'message' => 'Maximum photo size is 5 MB'
            ], 400);
        }

        // 5. Detect MIME type using PHP fileinfo
        if (!function_exists('finfo_open')) {
            return $this->json([
                'success' => false,
                'message' => 'PHP fileinfo extension is unavailable'
            ], 500);
        }

        $finfo = finfo_open(FILEINFO_MIME_TYPE);

        if ($finfo === false) {
            return $this->json([
                'success' => false,
                'message' => 'Cannot initialize MIME detection'
            ], 500);
        }

        $mimeType = finfo_file(
            $finfo,
            $photoFile->getPathname()
        );

        finfo_close($finfo);

        // 6. Allow only JPEG, PNG and WEBP
        $allowedTypes = [
            'image/jpeg' => 'jpg',
            'image/png'  => 'png',
            'image/webp' => 'webp'
        ];

        if (!isset($allowedTypes[$mimeType])) {
            return $this->json([
                'success' => false,
                'message' => 'Only JPEG, PNG and WEBP images are allowed',
                'detected_type' => $mimeType
            ], 400);
        }

        // 7. Create upload directory
        $uploadDir = $this->getParameter('kernel.project_dir')
            . '/public/uploads/annonces';

        if (!is_dir($uploadDir)) {
            if (!mkdir($uploadDir, 0775, true)
                && !is_dir($uploadDir)) {

                return $this->json([
                    'success' => false,
                    'message' => 'Cannot create upload directory'
                ], 500);
            }
        }

        // 8. Generate a unique filename
        try {
            $filename = bin2hex(random_bytes(16))
                . '.' . $allowedTypes[$mimeType];
        } catch (\Throwable $e) {
            return $this->json([
                'success' => false,
                'message' => 'Cannot generate photo filename'
            ], 500);
        }

        // 9. Move photo to public/uploads/annonces
        try {
            $photoFile->move($uploadDir, $filename);
        } catch (\Throwable $e) {
            return $this->json([
                'success' => false,
                'message' => 'Cannot save photo: ' . $e->getMessage()
            ], 500);
        }

        // 10. Save photo information in database
        $photoUrl = '/uploads/annonces/' . $filename;

        try {
            $photo = new PhotoAnnonce();

            $photo->setUrlPhoto($photoUrl);
            $photo->setAnnonce($annonce);

            $entityManager->persist($photo);
            $entityManager->flush();

        } catch (\Throwable $e) {

            // Remove uploaded file if database insertion fails
            $savedPath = $uploadDir . '/' . $filename;

            if (file_exists($savedPath)) {
                unlink($savedPath);
            }

            return $this->json([
                'success' => false,
                'message' => 'Database error: ' . $e->getMessage()
            ], 500);
        }

        // 11. Return success
        return $this->json([
            'success' => true,
            'message' => 'Photo uploaded successfully',
            'photo_id' => $photo->getId(),
            'annonce_id' => $annonce->getId(),
            'photo_url' => $photoUrl
        ], 201);
    }
}
